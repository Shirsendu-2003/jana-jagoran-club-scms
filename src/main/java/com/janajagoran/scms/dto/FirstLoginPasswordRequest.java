package com.janajagoran.scms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FirstLoginPasswordRequest {
    @NotBlank
    private String newPassword;
}
