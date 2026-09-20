package com.janajagoran.scms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HomepageContentRequest {
    @NotBlank
    private String sectionKey;

    private String title;
    private String body;
    private String imageUrl;
}
