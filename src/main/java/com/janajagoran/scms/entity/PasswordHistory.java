package com.janajagoran.scms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Records every password hash a user has had, purely for audit trail (WHO changed it, WHEN, HOW -- never for reuse comparisons of plaintext). */
@Entity
@Table(name = "password_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PasswordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** e.g. SELF_CHANGE, ADMIN_RESET, OTP_VERIFIED, FIRST_LOGIN, FORGOT_PASSWORD */
    @Column(length = 50)
    private String changeMethod;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }
}
