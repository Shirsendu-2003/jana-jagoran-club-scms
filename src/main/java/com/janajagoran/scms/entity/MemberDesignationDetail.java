package com.janajagoran.scms.entity;

import com.janajagoran.scms.enums.MemberDesignationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** member_desg_details -- who currently holds (or is pending approval for) a designation. At most one ACTIVE holder per designation. */
@Entity
@Table(name = "member_desg_details")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MemberDesignationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "designation_id", nullable = false)
    private DesignationMaster designation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MemberDesignationStatus status = MemberDesignationStatus.PENDING_APPROVAL;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requested_by")
    private User requestedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
    }
}
