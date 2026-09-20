package com.janajagoran.scms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoticeRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String type; // GENERAL, IMPORTANT, EMERGENCY, MEETING, EVENT

    private String attachmentUrl;
}
