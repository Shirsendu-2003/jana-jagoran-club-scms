package com.janajagoran.scms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Public-facing view of an APPROVED photo -- deliberately omits contact email, IP, and reviewer details. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoSubmissionResponse {
    private Long id;
    private String imageUrl;
    private String photographerName;
    private String caption;
    private String location;
    private LocalDateTime submittedAt;
    private Boolean isFeatured;
}
