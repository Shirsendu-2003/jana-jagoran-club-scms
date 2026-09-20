package com.janajagoran.scms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class IncomeRequest {
    private Long budgetId;

    @NotNull
    private String category; // MEMBERSHIP_FEE, DONATION, SPONSORSHIP, OTHER

    @NotNull
    private BigDecimal amount;

    private String description;

    @NotNull
    private LocalDate incomeDate;
}
