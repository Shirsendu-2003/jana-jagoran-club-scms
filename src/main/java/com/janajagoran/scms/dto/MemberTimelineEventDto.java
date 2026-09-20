package com.janajagoran.scms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** One entry in a member's chronological timeline, assembled from audit_logs + member_desg_history -- never a manually stored blob. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberTimelineEventDto {
    private LocalDateTime date;
    private String eventType; // e.g. REGISTERED, APPROVED, DESIGNATION_ASSIGNED, DESIGNATION_RELEASED, ACTIVATED, DEACTIVATED
    private String description;
    private String performedBy;
}
