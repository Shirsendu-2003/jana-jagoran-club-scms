package com.janajagoran.scms.service;

import com.janajagoran.scms.dto.*;
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
import com.janajagoran.scms.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final com.janajagoran.scms.repository.PasswordHistoryRepository passwordHistoryRepository;

    public RegistrationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists");
        }

        Role memberRole = roleRepository.findByName(RoleName.MEMBER)
                .orElseThrow(() -> new ResourceNotFoundException("MEMBER role not configured"));

        // New self-registrations start deactivated and PENDING until an Admin approves them.
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(memberRole)
                .isActive(false)
                .build();
        user = userRepository.save(user);

        String membershipId = "JJC"
                + LocalDateTime.now().getYear()
                + String.format("%03d", user.getId());
        Member member = Member.builder()
                .user(user)
                .membershipId(membershipId)
                .address(request.getAddress())
                .status(MemberStatus.PENDING)
                .build();
        memberRepository.save(member);

        auditLogService.log(AuditAction.MEMBER_REGISTERED, null, "Member", member.getId(),
                "New member self-registered, pending Admin approval", null, MemberStatus.PENDING.name(),
                user.getId(), user.getName());

        // No JWT is issued here -- the account cannot log in until an Admin approves it.
        return RegistrationResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .membershipId(membershipId)
                .status(MemberStatus.PENDING.name())
                .message("Registration received! An Admin will review your account before you can sign in. " +
                        "You'll be able to log in as soon as it's approved.")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadRequestException(inactiveAccountMessage(user));
        }

        String token = jwtUtil.generateToken(user.getEmail(), "ROLE_" + user.getRole().getName().name(), user.getId());

        auditLogService.log(AuditAction.LOGIN, user, "User", user.getId(),
                "User logged in", null, null, user.getId(), user.getName());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().getName().name())
                .profilePicture(user.getProfilePicture())
                .mustChangePassword(Boolean.TRUE.equals(user.getMustChangePassword()))
                .build();
    }

    /** Gives a precise, approval-aware message instead of a generic "deactivated" error. */
    private String inactiveAccountMessage(User user) {
        return memberRepository.findByUserId(user.getId())
                .map(member -> switch (member.getStatus()) {
                    case PENDING -> "Your registration is still awaiting Admin approval. Please check back soon.";
                    case REJECTED -> "Your registration was not approved. Please contact the club administrator.";
                    default -> "Your account has been deactivated. Please contact the club administrator.";
                })
                .orElse("Your account has been deactivated. Please contact the club administrator.");
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        recordPasswordChange(user, "SELF_CHANGE", user);
        auditLogService.log(AuditAction.PASSWORD_CHANGED, user, "User", user.getId(),
                "Password changed via current-password verification", null, null, user.getId(), user.getName());
    }

    private void recordPasswordChange(User user, String method, User changedBy) {
        passwordHistoryRepository.save(com.janajagoran.scms.entity.PasswordHistory.builder()
                .user(user)
                .passwordHash(user.getPassword())
                .changeMethod(method)
                .changedBy(changedBy)
                .build());
    }

    /** Used right after login when mustChangePassword=true (temp password from Admin, or first login) --
     *  no "current password" is required since the user is proving identity via the still-valid temp password's JWT. */
    public void firstLoginSetPassword(Long userId, FirstLoginPasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        recordPasswordChange(user, "FIRST_LOGIN", user);
        auditLogService.log(AuditAction.PASSWORD_CHANGED, user, "User", user.getId(),
                "Password set on first login", null, null, user.getId(), user.getName());
    }

    /** Step 1 of the OTP-verified password change: generates a 6-digit code and delivers it via
     *  an in-app notification (no SMS/email gateway wired in -- see NotificationService). */
    public OtpRequestResponse requestPasswordChangeOtp(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String otp = String.format("%06d", new java.security.SecureRandom().nextInt(1_000_000));
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        notificationService.notifyUser(user, "Password Change OTP",
                "Your OTP to change your password is " + otp + ". It expires in 10 minutes. " +
                        "Do not share this code with anyone.",
                NotificationType.NOTICE);

        return OtpRequestResponse.builder()
                .message("An OTP has been sent to your notifications. Check the bell icon to retrieve it.")
                .expiresInMinutes(10)
                .build();
    }

    /** Step 2: confirm the OTP and set the new password. */
    public void confirmPasswordChangeOtp(Long userId, OtpConfirmRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getOtpCode() == null || !user.getOtpCode().equals(request.getOtpCode())) {
            throw new BadRequestException("Incorrect OTP code");
        }
        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        user.setMustChangePassword(false);
        userRepository.save(user);

        recordPasswordChange(user, "OTP_VERIFIED", user);
        auditLogService.log(AuditAction.PASSWORD_CHANGED, user, "User", user.getId(),
                "Password changed via OTP verification", null, null, user.getId(), user.getName());
    }

    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // In production: email this token/link to the user via JavaMailSender.
        // Returned here directly so the frontend/demo can proceed without SMTP configured.
        return token;
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        recordPasswordChange(user, "FORGOT_PASSWORD", user);
        auditLogService.log(AuditAction.PASSWORD_RESET, user, "User", user.getId(),
                "Password reset via forgot-password flow", null, null, user.getId(), user.getName());
    }
}
