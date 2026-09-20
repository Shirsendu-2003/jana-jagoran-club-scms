package com.janajagoran.scms.entity;

import com.janajagoran.scms.enums.AuditAction;
import com.janajagoran.scms.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Centralized, append-only audit trail. Every sensitive operation (registration, approval,
 * login, password change, activation/deactivation, designation lifecycle, role change, ...)
 * writes one row here. Never physically deleted -- see AuditLogService.
 */
@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(length = 2000)
    private String description;

    @Column(name = "old_value", length = 1000)
    private String oldValue;

    @Column(name = "new_value", length = 1000)
    private String newValue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "performed_by")
    private User performedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "performed_by_role", length = 50)
    private RoleName performedByRole;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "target_user_name", length = 150)
    private String targetUserName;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
