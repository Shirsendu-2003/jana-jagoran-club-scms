package com.janajagoran.scms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetRequest {
    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Integer fiscalYear;

    private BigDecimal totalAmount;
}
