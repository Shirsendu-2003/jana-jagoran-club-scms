package com.janajagoran.scms.entity;

import com.janajagoran.scms.enums.PaymentMode;
import com.janajagoran.scms.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "payment_month", "payment_year"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "payment_month", nullable = false)
    private Integer paymentMonth;

    @Column(name = "payment_year", nullable = false)
    private Integer paymentYear;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal amount = new BigDecimal("50.00");

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.UNPAID;

    @Column(name = "paid_on")
    private LocalDateTime paidOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode")
    @Builder.Default
    private PaymentMode paymentMode = PaymentMode.CASH;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recorded_by")
    private User recordedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
