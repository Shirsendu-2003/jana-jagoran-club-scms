package com.janajagoran.scms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DesignationRequest {
    @NotBlank
    private String title;

    private String description;

    private String level; // CHAIR, LOWER_LEVEL
}
