package com.janajagoran.scms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private long totalMembers;
    private long activeMembers;
    private long pendingPayments;
    private long pendingRegistrations;
    private long totalEvents;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;
    private BigDecimal monthlyCollected;
}
