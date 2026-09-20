package com.janajagoran.scms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Flattened, dashboard-ready view of a MemberDesignationHistory episode, with duration computed on read (never stored). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentHistoryDto {
    private Long historyId;
    private Long memberId;
    private String memberName;
    private String membershipId;
    private String designationTitle;
    private String assignedBy;
    private String assignedByRole;
    private String approvedBy;
    private String approvedByRole;
    private LocalDateTime startDate;
    private LocalDateTime endDate; // null = still active
    private Long durationDays;
    private String status; // ACTIVE, RELEASED, REJECTED
    private String reason;
}
