package com.janajagoran.scms.repository;

import com.janajagoran.scms.entity.ApprovalRequest;
import com.janajagoran.scms.enums.ApprovalRequestStatus;
import com.janajagoran.scms.enums.ApprovalRequestType;
import com.janajagoran.scms.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {
    List<ApprovalRequest> findByStatus(ApprovalRequestStatus status);
    List<ApprovalRequest> findByStatusAndAssignedToRole(ApprovalRequestStatus status, RoleName role);
    List<ApprovalRequest> findByEntityTypeAndEntityId(String entityType, Long entityId);
    Optional<ApprovalRequest> findByEntityTypeAndEntityIdAndStatus(String entityType, Long entityId, ApprovalRequestStatus status);
    Page<ApprovalRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<ApprovalRequest> findByRequestType(ApprovalRequestType type);
}
