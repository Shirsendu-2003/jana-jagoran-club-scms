package com.janajagoran.scms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank
    private String name;

    @NotBlank @Email
    private String email;

    private String phone;

    @NotBlank
    private String password;

    @NotBlank
    private String role; // MEMBER, SECRETARY, PRESIDENT, ADMIN, SUPER_ADMIN

    private String address;
}
