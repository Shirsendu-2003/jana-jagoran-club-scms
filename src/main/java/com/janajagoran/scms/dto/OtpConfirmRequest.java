package com.janajagoran.scms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpConfirmRequest {
    @NotBlank
    private String otpCode;

    @NotBlank
    private String newPassword;
}
