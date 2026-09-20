package com.janajagoran.scms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FeedbackRequest {
    private String type; // SUGGESTION, ISSUE

    @NotBlank
    private String message;
}
