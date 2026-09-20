package com.janajagoran.scms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRecordRequest {
    @NotNull
    private Long memberId;

    @NotNull
    private Integer month;

    @NotNull
    private Integer year;

    private BigDecimal amount;

    private String paymentMode; // CASH, ONLINE, MANUAL
}
