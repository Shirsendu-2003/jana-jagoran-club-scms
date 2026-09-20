package com.janajagoran.scms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Auditable record of OTP requests/verifications (password reset, password change), separate from the live otp_code on User. */
@Entity
@Table(name = "otp_verifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** e.g. PASSWORD_CHANGE, PASSWORD_RESET */
    @Column(length = 50, nullable = false)
    private String purpose;

    @Column(name = "otp_code", nullable = false, length = 10)
    private String otpCode;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        requestedAt = LocalDateTime.now();
    }
}
