package com.janajagoran.scms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberPaymentSummaryDto {
    private Long memberId;
    private String membershipId;
    private String memberName;
    private BigDecimal totalDue;
    private int pendingMonthsCount;
    private List<String> pendingMonths;
}
