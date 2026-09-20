package com.janajagoran.scms.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String phone;
    private String email;
    private String address;
}
