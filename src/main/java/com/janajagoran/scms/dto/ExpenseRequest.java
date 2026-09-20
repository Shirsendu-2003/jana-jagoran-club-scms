package com.janajagoran.scms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseRequest {
    private Long budgetId;

    @NotNull
    private String category; // PUJA, DECORATION, FOOD, ELECTRICITY, MAINTENANCE, MISCELLANEOUS

    @NotNull
    private BigDecimal amount;

    private String description;

    @NotNull
    private LocalDate expenseDate;
}
