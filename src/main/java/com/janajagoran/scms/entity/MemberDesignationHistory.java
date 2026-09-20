package com.janajagoran.scms.entity;

import com.janajagoran.scms.enums.DesignationHistoryAction;
import com.janajagoran.scms.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * member_desg_history -- ONE ROW PER ASSIGNMENT EPISODE (not one row per event).
 * Created when a member is ASSIGNED to a designation (assignmentDate set), then the SAME
 * row is updated in place with releaseDate when they're later RELEASED -- never deleted,
 * so "who held what, when, for how long" is always a single row + a duration_days calc.
 * Rows are also created standalone for REJECTED assignment requests (no release fields).
 */
@Entity
@Table(name = "member_desg_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MemberDesignationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** Snapshot so this row still reads correctly even if the member's name changes later. */
    @Column(name = "member_name", length = 150)
    private String memberName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "designation_id", nullable = false)
    private DesignationMaster designation;

    /** Only set for TRANSFERRED episodes -- what they held immediately before this one. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "previous_designation_id")
    private DesignationMaster previousDesignation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DesignationHistoryAction action;

    @Column(length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_by_role", length = 50)
    private RoleName assignedByRole;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "approved_by_role", length = 50)
    private RoleName approvedByRole;

    /** Start of this episode (when the assignment was approved and became active). */
    @Column(name = "assignment_date")
    private LocalDateTime assignmentDate;

    /** End of this episode -- null while still active; set when released. Duration is
     *  always computed dynamically from these two (never stored) -- see AssignmentHistoryDto. */
    @Column(name = "release_date")
    private LocalDateTime releaseDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
