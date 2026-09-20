package com.janajagoran.scms.service;

import com.janajagoran.scms.entity.ApprovalAction;
import com.janajagoran.scms.entity.ApprovalRequest;
import com.janajagoran.scms.entity.User;
import com.janajagoran.scms.enums.ApprovalDecision;
import com.janajagoran.scms.enums.ApprovalRequestStatus;
import com.janajagoran.scms.enums.ApprovalRequestType;
import com.janajagoran.scms.enums.RoleName;
import com.janajagoran.scms.exception.BadRequestException;
import com.janajagoran.scms.exception.ResourceNotFoundException;
import com.janajagoran.scms.repository.ApprovalActionRepository;
import com.janajagoran.scms.repository.ApprovalRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Generic approval engine. Any sensitive action opens an ApprovalRequest here rather than
 * mutating state directly; the caller supplies a Runnable that performs the actual state
 * change, which only executes once ApprovalAction.decision = APPROVED is recorded -- both
 * happen in the same transaction as the ApprovalRequest status update.
 *
 * This does NOT replace domain-specific services (DesignationService, UserService) -- it's
 * the shared "open a request, decide on it" primitive they call into, so every workflow gets
 * the same auditable request/decision trail without duplicating that logic five times.
 */
@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalRequestRepository requestRepository;
    private final ApprovalActionRepository actionRepository;

    @Transactional
    public ApprovalRequest open(ApprovalRequestType type, String entityType, Long entityId,
                                 User requestedBy, RoleName assignedToRole, String remarks, String payload) {
        ApprovalRequest request = ApprovalRequest.builder()
                .requestType(type)
                .entityType(entityType)
                .entityId(entityId)
                .requestedBy(requestedBy)
                .requestedByRole(requestedBy != null && requestedBy.getRole() != null ? requestedBy.getRole().getName() : null)
                .assignedToRole(assignedToRole)
                .status(ApprovalRequestStatus.PENDING)
                .remarks(remarks)
                .payload(payload)
                .build();
        return requestRepository.save(request);
    }

    /**
     * Decide on a request. If approved, runs `onApprove` (the actual state mutation) before
     * marking the request COMPLETED -- same transaction, so a failure in onApprove rolls back
     * the approval too. If rejected, `onApprove` is never called.
     */
    @Transactional
    public ApprovalRequest decide(Long requestId, boolean approve, User decidedBy, String remarks, Runnable onApprove) {
        ApprovalRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found"));

        if (request.getStatus() != ApprovalRequestStatus.PENDING) {
            throw new BadRequestException("This request has already been decided");
        }

        ApprovalAction action = ApprovalAction.builder()
                .request(request)
                .decision(approve ? ApprovalDecision.APPROVED : ApprovalDecision.REJECTED)
                .decidedBy(decidedBy)
                .decidedByRole(decidedBy != null && decidedBy.getRole() != null ? decidedBy.getRole().getName() : null)
                .remarks(remarks)
                .build();
        actionRepository.save(action);

        if (approve) {
            if (onApprove != null) onApprove.run();
            request.setStatus(ApprovalRequestStatus.COMPLETED);
            request.setApprovedAt(LocalDateTime.now());
            request.setCompletedAt(LocalDateTime.now());
        } else {
            request.setStatus(ApprovalRequestStatus.REJECTED);
            request.setRejectedAt(LocalDateTime.now());
        }

        return requestRepository.save(request);
    }

    @Transactional
    public void cancel(Long requestId) {
        ApprovalRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found"));
        if (request.getStatus() != ApprovalRequestStatus.PENDING) {
            throw new BadRequestException("Only a pending request can be cancelled");
        }
        request.setStatus(ApprovalRequestStatus.CANCELLED);
        requestRepository.save(request);
    }

    public List<ApprovalRequest> getPendingForRole(RoleName role) {
        return requestRepository.findByStatusAndAssignedToRole(ApprovalRequestStatus.PENDING, role);
    }

    public List<ApprovalRequest> getAllPending() {
        return requestRepository.findByStatus(ApprovalRequestStatus.PENDING);
    }

    public List<ApprovalRequest> getForEntity(String entityType, Long entityId) {
        return requestRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    public Page<ApprovalRequest> getAll(Pageable pageable) {
        return requestRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public List<ApprovalAction> getActionsForRequest(Long requestId) {
        return actionRepository.findByRequestId(requestId);
    }
}
