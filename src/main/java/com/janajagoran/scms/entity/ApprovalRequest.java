package com.janajagoran.scms.entity;

import com.janajagoran.scms.enums.ApprovalRequestStatus;
import com.janajagoran.scms.enums.ApprovalRequestType;
import com.janajagoran.scms.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Generic approval engine: every sensitive action (member registration, designation
 * assign/release, Secretary/President assignment, activation/deactivation, role change)
 * opens one of these instead of mutating state directly. The actual state change only
 * happens once an ApprovalAction with decision=APPROVED is recorded (see ApprovalService).
 */
@Entity
@Table(name = "approval_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 50)
    private ApprovalRequestType requestType;

    /** Logical entity this request concerns, e.g. "Member", "MemberDesignationDetail". */
    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requested_by")
    private User requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_by_role", length = 50)
    private RoleName requestedByRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_to_role", length = 50)
    private RoleName assignedToRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ApprovalRequestStatus status = ApprovalRequestStatus.PENDING;

    @Column(length = 1000)
    private String remarks;

    /** Free-form context needed to apply the change once approved (e.g. "designationId=4"). */
    @Column(name = "payload", length = 1000)
    private String payload;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
