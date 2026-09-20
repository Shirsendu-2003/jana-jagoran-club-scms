package com.janajagoran.scms.entity;

import com.janajagoran.scms.enums.ApprovalDecision;
import com.janajagoran.scms.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** The actual decision made on an ApprovalRequest -- kept separate so a request could
 *  in principle require multiple sign-offs later without changing the schema. */
@Entity
@Table(name = "approval_actions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "request_id", nullable = false)
    private ApprovalRequest request;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalDecision decision;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "decided_by")
    private User decidedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "decided_by_role", length = 50)
    private RoleName decidedByRole;

    @Column(length = 1000)
    private String remarks;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @PrePersist
    protected void onCreate() {
        decidedAt = LocalDateTime.now();
    }
}
