package com.janajagoran.scms.entity;

import com.janajagoran.scms.enums.IncomeCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "income")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Income {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "budget_id")
    private Budget budget;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncomeCategory category;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(length = 500)
    private String description;

    @Column(name = "income_date", nullable = false)
    private LocalDate incomeDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recorded_by", nullable = false)
    private User recordedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
