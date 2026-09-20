package com.janajagoran.scms.service;

import com.janajagoran.scms.dto.AdminResetPasswordResponse;
import com.janajagoran.scms.dto.CreateUserRequest;
import com.janajagoran.scms.dto.UpdateProfileRequest;
import com.janajagoran.scms.dto.UserProfileDto;
import com.janajagoran.scms.entity.Member;
import com.janajagoran.scms.entity.Role;
import com.janajagoran.scms.entity.User;
import com.janajagoran.scms.enums.AuditAction;
import com.janajagoran.scms.enums.MemberStatus;
import com.janajagoran.scms.enums.NotificationType;
import com.janajagoran.scms.enums.RoleName;
import com.janajagoran.scms.exception.BadRequestException;
import com.janajagoran.scms.exception.ResourceNotFoundException;
import com.janajagoran.scms.repository.MemberRepository;
import com.janajagoran.scms.repository.RoleRepository;
import com.janajagoran.scms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final com.janajagoran.scms.repository.PasswordHistoryRepository passwordHistoryRepository;

    /** Lightweight managed reference, safe to use as a foreign-key link on other entities without an extra SELECT. */
    public User getReference(Long userId) {
        return userRepository.getReferenceById(userId);
    }

    public UserProfileDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserProfileDto.UserProfileDtoBuilder builder = UserProfileDto.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().getName().name())
                .profilePicture(user.getProfilePicture())
                .isActive(user.getIsActive());

        Optional<Member> memberOpt = memberRepository.findByUserId(userId);
        memberOpt.ifPresent(m -> builder
                .memberId(m.getId())
                .membershipId(m.getMembershipId())
                .address(m.getAddress())
                .dateOfJoining(m.getDateOfJoining())
                .monthlyFee(m.getMonthlyFee())
                .status(m.getStatus().name()));

        return builder.build();
    }

    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getName() != null && !request.getName().isBlank()) user.setName(request.getName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getEmail() != null && !request.getEmail().isBlank() && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("That email is already in use by another account");
            }
            user.setEmail(request.getEmail());
        }
        userRepository.save(user);

        if (request.getAddress() != null) {
            memberRepository.findByUserId(userId).ifPresent(m -> {
                m.setAddress(request.getAddress());
                memberRepository.save(m);
            });
        }
    }

    @Transactional
    public void updateProfilePicture(Long userId, String url) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setProfilePicture(url);
        userRepository.save(user);
    }

    // ---------------- Admin: user management ----------------

    @Transactional
    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("A user with this email already exists");
        }
        RoleName roleName;
        try {
            roleName = RoleName.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + request.getRole());
        }
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not configured: " + roleName));

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .isActive(true)
                .mustChangePassword(true) // Admin set this temp password -- force them to pick their own on first login
                .build();
        user = userRepository.save(user);

        if (roleName == RoleName.MEMBER) {
            String membershipId = "JJC"
                    + LocalDateTime.now().getYear()
                    + String.format("%03d", user.getId());
            Member member = Member.builder()
                    .user(user)
                    .membershipId(membershipId)
                    .address(request.getAddress())
                    .status(MemberStatus.ACTIVE)
                    .build();
            memberRepository.save(member);
        }

        return user;
    }

    /** "Admin Profile Edit(Search User ---> Password Reset ---> User Id ---> Password)":
     *  generates a fresh temporary password, forces the user to change it on next login,
     *  and returns it to the Admin to relay through a trusted channel (no SMS/email gateway wired in). */
    @Transactional
    public AdminResetPasswordResponse resetUserPassword(Long userId, User performedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String tempPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);

        passwordHistoryRepository.save(com.janajagoran.scms.entity.PasswordHistory.builder()
                .user(user)
                .passwordHash(user.getPassword())
                .changeMethod("ADMIN_RESET")
                .changedBy(performedBy)
                .build());

        auditLogService.log(AuditAction.PASSWORD_RESET, performedBy, "User", user.getId(),
                "Admin reset password for user", null, null, user.getId(), user.getName());

        return AdminResetPasswordResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .temporaryPassword(tempPassword)
                .message("Password reset. Share this temporary password with the user through a trusted channel " +
                        "-- they'll be required to set their own on next login.")
                .build();
    }

    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder("Jjc@");
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Transactional
    public User updateUserRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        RoleName roleName;
        try {
            roleName = RoleName.valueOf(newRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + newRole);
        }
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not configured"));
        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public User setActiveStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setIsActive(active);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Member> searchMembers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return memberRepository.findAll();
        }
        return memberRepository.search(keyword);
    }

    @Transactional
    public Member setMemberStatus(Long memberId, MemberStatus status) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        member.setStatus(status);
        return memberRepository.save(member);
    }

    /** "Member Active/Inactive -> table -> switch -> modal reason -> save":
     *  toggling to Inactive requires and stores a reason; toggling back to Active clears it. */
    @Transactional
    public Member toggleMemberActiveStatus(Long memberId, boolean active, String reason, User performedBy) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (!active && (reason == null || reason.isBlank())) {
            throw new BadRequestException("A reason is required when marking a member Inactive");
        }

        member.setStatus(active ? MemberStatus.ACTIVE : MemberStatus.INACTIVE);
        member.setDeactivationReason(active ? null : reason);
        memberRepository.save(member);

        member.getUser().setIsActive(active);
        userRepository.save(member.getUser());

        auditLogService.log(
                active ? AuditAction.MEMBER_ACTIVATED : AuditAction.MEMBER_DEACTIVATED,
                performedBy, "Member", member.getId(),
                active ? "Member reactivated" : ("Member deactivated. Reason: " + reason),
                active ? "INACTIVE" : "ACTIVE", member.getStatus().name(),
                member.getUser().getId(), member.getUser().getName());

        return member;
    }

    public List<Member> getPendingMembers() {
        return memberRepository.findByStatus(MemberStatus.PENDING);
    }

    @Transactional
    public Member approveMember(Long memberId, User performedBy) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        member.setStatus(MemberStatus.ACTIVE);
        memberRepository.save(member);

        User user = member.getUser();
        user.setIsActive(true);
        userRepository.save(user);

        notificationService.notifyUser(user, "Registration Approved",
                "Welcome to Jana Jagoran Club! Your account has been approved -- you can now log in.",
                NotificationType.NOTICE);

        auditLogService.log(AuditAction.MEMBER_APPROVED, performedBy, "Member", member.getId(),
                "Member registration approved", MemberStatus.PENDING.name(), MemberStatus.ACTIVE.name(),
                user.getId(), user.getName());

        return member;
    }

    @Transactional
    public Member rejectMember(Long memberId, User performedBy) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        member.setStatus(MemberStatus.REJECTED);
        memberRepository.save(member);

        User user = member.getUser();
        user.setIsActive(false);
        userRepository.save(user);

        auditLogService.log(AuditAction.MEMBER_REJECTED, performedBy, "Member", member.getId(),
                "Member registration rejected", MemberStatus.PENDING.name(), MemberStatus.REJECTED.name(),
                user.getId(), user.getName());

        return member;
    }
}
