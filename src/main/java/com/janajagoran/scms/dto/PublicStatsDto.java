package com.janajagoran.scms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Only non-sensitive, aggregate numbers -- safe to expose without authentication. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicStatsDto {
    private long activeMembers;
    private long totalEvents;
    private long photosShared;
}
