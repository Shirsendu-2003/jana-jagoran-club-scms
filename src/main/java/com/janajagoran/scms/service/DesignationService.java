package com.janajagoran.scms.service;

import com.janajagoran.scms.entity.*;
import com.janajagoran.scms.enums.*;
import com.janajagoran.scms.exception.BadRequestException;
import com.janajagoran.scms.exception.ResourceNotFoundException;
import com.janajagoran.scms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DesignationService {

    private final DesignationMasterRepository designationRepository;
    private final DesignationPostDetailRepository postDetailRepository;
    private final DesignationPermissionRepository permissionRepository;
    private final MemberDesignationDetailRepository memberDesignationRepository;
    private final MemberDesignationHistoryRepository historyRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final ApprovalService approvalService;

    private static final String ENTITY_POST_DETAIL = "DesignationPostDetail";
    private static final String ENTITY_MEMBER_DESIGNATION = "MemberDesignationDetail";

    @Transactional
    public DesignationMaster proposeDesignation(com.janajagoran.scms.dto.DesignationRequest request, User operator) {
        if (designationRepository.existsByTitleIgnoreCase(request.getTitle())) {
            throw new BadRequestException("A designation with this title already exists");
        }

        DesignationLevel level = DesignationLevel.LOWER_LEVEL;
        if (request.getLevel() != null) {
            try {
                level = DesignationLevel.valueOf(request.getLevel().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid level: " + request.getLevel());
            }
        }

        DesignationMaster designation = DesignationMaster.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .level(level)
                .createdBy(operator)
                .build();
        designation = designationRepository.save(designation);

        DesignationPostDetail postDetail = DesignationPostDetail.builder()
                .designation(designation)
                .status(DesignationStatus.CREATION)
                .build();
        postDetail = postDetailRepository.save(postDetail);

        approvalService.open(ApprovalRequestType.DESIGNATION_CREATION, ENTITY_POST_DETAIL, postDetail.getId(),
                operator, RoleName.ADMIN, "Proposed designation: " + designation.getTitle(), null);

        return designation;
    }

    @Transactional
    public DesignationPostDetail decideDesignation(Long designationId, boolean approve, User admin, String remarks) {
        DesignationPostDetail postDetail = getPostDetail(designationId);

        if (postDetail.getStatus() != DesignationStatus.CREATION) {
            throw new BadRequestException("Only designations awaiting creation approval can be decided on");
        }

        ApprovalRequest request = findPendingRequest(ApprovalRequestType.DESIGNATION_CREATION, ENTITY_POST_DETAIL, postDetail.getId());

        approvalService.decide(request.getId(), approve, admin, remarks, () -> {
            postDetail.setStatus(DesignationStatus.ACTIVE);
            postDetail.setApprovedBy(admin);
            postDetail.setApprovedAt(LocalDateTime.now());
            postDetailRepository.save(postDetail);
        });

        if (!approve) {
            postDetail.setStatus(DesignationStatus.INACTIVE);
            postDetail.setApprovedBy(admin);
            postDetail.setApprovedAt(LocalDateTime.now());
            postDetailRepository.save(postDetail);
        }

        return postDetail;
    }

    @Transactional
    public DesignationPostDetail retireDesignation(Long designationId, User admin) {
        DesignationPostDetail postDetail = getPostDetail(designationId);
        if (postDetail.getStatus() != DesignationStatus.ACTIVE) {
            throw new BadRequestException("Only an active designation can be retired");
        }
        if (memberDesignationRepository.findByDesignationIdAndStatus(designationId, MemberDesignationStatus.ACTIVE).isPresent()) {
            throw new BadRequestException("Release the current holder before retiring this designation");
        }
        postDetail.setStatus(DesignationStatus.INACTIVE);
        postDetail.setApprovedBy(admin);
        postDetail.setApprovedAt(LocalDateTime.now());
        return postDetailRepository.save(postDetail);
    }

    public List<DesignationMaster> getAllDesignations() {
        return designationRepository.findAll();
    }

    public DesignationPostDetail getPostDetail(Long designationId) {
        return postDetailRepository.findByDesignationId(designationId)
                .orElseThrow(() -> new ResourceNotFoundException("Designation post details not found"));
    }

    public List<DesignationPermission> setMenuPermissions(Long designationId, List<MenuKey> menuKeys) {
        DesignationPostDetail postDetail = getPostDetail(designationId);
        permissionRepository.deleteByPostDetailId(postDetail.getId());
        return menuKeys.stream()
                .map(key -> permissionRepository.save(DesignationPermission.builder().postDetail(postDetail).menuKey(key).build()))
                .toList();
    }

    public List<DesignationPermission> getMenuPermissions(Long designationId) {
        DesignationPostDetail postDetail = getPostDetail(designationId);
        return permissionRepository.findByPostDetailId(postDetail.getId());
    }

    @Transactional
    public MemberDesignationDetail requestAssignment(Long memberId, Long designationId, User requestedBy) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        DesignationPostDetail postDetail = getPostDetail(designationId);

        if (postDetail.getStatus() != DesignationStatus.ACTIVE) {
            throw new BadRequestException("This designation is not active and cannot be assigned");
        }
        if (memberDesignationRepository.findByDesignationIdAndStatus(designationId, MemberDesignationStatus.ACTIVE).isPresent()) {
            throw new BadRequestException("This designation is already held by someone -- release it first");
        }

        MemberDesignationDetail detail = MemberDesignationDetail.builder()
                .member(member)
                .designation(postDetail.getDesignation())
                .status(MemberDesignationStatus.PENDING_APPROVAL)
                .requestedBy(requestedBy)
                .build();
        detail = memberDesignationRepository.save(detail);

        approvalService.open(ApprovalRequestType.DESIGNATION_ASSIGN, ENTITY_MEMBER_DESIGNATION, detail.getId(),
                requestedBy, RoleName.ADMIN,
                "Assign " + member.getUser().getName() + " to " + postDetail.getDesignation().getTitle(), null);

        return detail;
    }

    @Transactional
    public MemberDesignationDetail decideAssignment(Long memberDesignationId, boolean approve, User approver, String remarks) {
        MemberDesignationDetail detail = memberDesignationRepository.findById(memberDesignationId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment request not found"));

        if (detail.getStatus() != MemberDesignationStatus.PENDING_APPROVAL) {
            throw new BadRequestException("This request has already been decided");
        }

        ApprovalRequest request = findPendingRequest(ApprovalRequestType.DESIGNATION_ASSIGN, ENTITY_MEMBER_DESIGNATION, detail.getId());

        approvalService.decide(request.getId(), approve, approver, remarks, () -> {
            detail.setStatus(MemberDesignationStatus.ACTIVE);
            detail.setApprovedBy(approver);
            detail.setApprovedAt(LocalDateTime.now());
            memberDesignationRepository.save(detail);

            MemberDesignationHistory episode = MemberDesignationHistory.builder()
                    .member(detail.getMember())
                    .memberName(detail.getMember().getUser().getName())
                    .designation(detail.getDesignation())
                    .action(DesignationHistoryAction.ASSIGNED)
                    .assignedBy(detail.getRequestedBy())
                    .assignedByRole(detail.getRequestedBy() != null && detail.getRequestedBy().getRole() != null ? detail.getRequestedBy().getRole().getName() : null)
                    .approvedBy(approver)
                    .approvedByRole(approver.getRole() != null ? approver.getRole().getName() : null)
                    .assignmentDate(LocalDateTime.now())
                    .build();
            historyRepository.save(episode);

            notificationService.notifyUser(detail.getMember().getUser(), "Designation Assigned",
                    "You've been assigned as " + detail.getDesignation().getTitle() + ". Congratulations!",
                    NotificationType.NOTICE);
        });

        if (!approve) {
            MemberDesignationHistory rejectedEpisode = MemberDesignationHistory.builder()
                    .member(detail.getMember())
                    .memberName(detail.getMember().getUser().getName())
                    .designation(detail.getDesignation())
                    .action(DesignationHistoryAction.REJECTED)
                    .reason(remarks)
                    .assignedBy(detail.getRequestedBy())
                    .assignedByRole(detail.getRequestedBy() != null && detail.getRequestedBy().getRole() != null ? detail.getRequestedBy().getRole().getName() : null)
                    .approvedBy(approver)
                    .approvedByRole(approver.getRole() != null ? approver.getRole().getName() : null)
                    .assignmentDate(LocalDateTime.now())
                    .releaseDate(LocalDateTime.now())
                    .build();
            historyRepository.save(rejectedEpisode);
            memberDesignationRepository.delete(detail);
        }

        return detail;
    }

    @Transactional
    public MemberDesignationDetail requestRelease(Long memberDesignationId, String reason, User requestedBy) {
        MemberDesignationDetail detail = memberDesignationRepository.findById(memberDesignationId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        if (detail.getStatus() != MemberDesignationStatus.ACTIVE) {
            throw new BadRequestException("Only an active assignment can be released");
        }

        approvalService.open(ApprovalRequestType.DESIGNATION_RELEASE, ENTITY_MEMBER_DESIGNATION, detail.getId(),
                requestedBy, RoleName.ADMIN,
                "Release " + detail.getMember().getUser().getName() + " from " + detail.getDesignation().getTitle() + ": " + reason,
                null);

        return detail;
    }

    @Transactional
    public void decideRelease(Long memberDesignationId, boolean approve, String reason, User approver) {
        MemberDesignationDetail detail = memberDesignationRepository.findById(memberDesignationId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        ApprovalRequest request = findPendingRequest(ApprovalRequestType.DESIGNATION_RELEASE, ENTITY_MEMBER_DESIGNATION, detail.getId());

        approvalService.decide(request.getId(), approve, approver, reason, () -> {
            historyRepository.findByMemberIdAndDesignationIdAndReleaseDateIsNull(detail.getMember().getId(), detail.getDesignation().getId())
                    .ifPresent(episode -> {
                        episode.setReleaseDate(LocalDateTime.now());
                        episode.setAction(DesignationHistoryAction.RELEASED);
                        if (reason != null && !reason.isBlank()) episode.setReason(reason);
                        historyRepository.save(episode);
                    });

            notificationService.notifyUser(detail.getMember().getUser(), "Designation Released",
                    "You have been released from the position of " + detail.getDesignation().getTitle() +
                            (reason != null ? (". Reason: " + reason) : "."),
                    NotificationType.NOTICE);

            memberDesignationRepository.delete(detail);
        });
    }

    private ApprovalRequest findPendingRequest(ApprovalRequestType type, String entityType, Long entityId) {
        return approvalService.getForEntity(entityType, entityId).stream()
                .filter(r -> r.getRequestType() == type && r.getStatus() == ApprovalRequestStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No pending approval request found for this action"));
    }

    public List<MemberDesignationDetail> getPendingAssignments() {
        return memberDesignationRepository.findByStatus(MemberDesignationStatus.PENDING_APPROVAL);
    }

    public List<MemberDesignationDetail> getActiveAssignments() {
        return memberDesignationRepository.findByStatus(MemberDesignationStatus.ACTIVE);
    }

    public List<MemberDesignationDetail> getMemberDesignations(Long memberId) {
        return memberDesignationRepository.findByMemberId(memberId);
    }

    public List<MemberDesignationHistory> getMemberHistory(Long memberId) {
        return historyRepository.findByMemberIdOrderByAssignmentDateDesc(memberId);
    }

    public List<MemberDesignationHistory> getAllHistory() {
        return historyRepository.findAll();
    }

    public List<com.janajagoran.scms.dto.AssignmentHistoryDto> getAssignmentHistoryDtos() {
        return historyRepository.findAllByOrderByAssignmentDateDesc(org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream().map(this::toDto).toList();
    }

    public List<com.janajagoran.scms.dto.AssignmentHistoryDto> getAssignmentHistoryForMember(Long memberId) {
        return historyRepository.findByMemberIdOrderByAssignmentDateDesc(memberId).stream().map(this::toDto).toList();
    }

    private com.janajagoran.scms.dto.AssignmentHistoryDto toDto(MemberDesignationHistory h) {
        LocalDateTime end = h.getReleaseDate();
        LocalDateTime start = h.getAssignmentDate();
        Long durationDays = start != null
                ? java.time.temporal.ChronoUnit.DAYS.between(start, end != null ? end : LocalDateTime.now())
                : null;

        String status = h.getAction() == DesignationHistoryAction.REJECTED ? "REJECTED"
                : end != null ? "RELEASED" : "ACTIVE";

        return com.janajagoran.scms.dto.AssignmentHistoryDto.builder()
                .historyId(h.getId())
                .memberId(h.getMember().getId())
                .memberName(h.getMemberName())
                .membershipId(h.getMember().getMembershipId())
                .designationTitle(h.getDesignation().getTitle())
                .assignedBy(h.getAssignedBy() != null ? h.getAssignedBy().getName() : null)
                .assignedByRole(h.getAssignedByRole() != null ? h.getAssignedByRole().name() : null)
                .approvedBy(h.getApprovedBy() != null ? h.getApprovedBy().getName() : null)
                .approvedByRole(h.getApprovedByRole() != null ? h.getApprovedByRole().name() : null)
                .startDate(start)
                .endDate(end)
                .durationDays(durationDays)
                .status(status)
                .reason(h.getReason())
                .build();
    }
}
