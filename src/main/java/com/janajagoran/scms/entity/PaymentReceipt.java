package com.janajagoran.scms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_receipts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Column(name = "receipt_number", nullable = false, unique = true, length = 100)
    private String receiptNumber;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }
}
