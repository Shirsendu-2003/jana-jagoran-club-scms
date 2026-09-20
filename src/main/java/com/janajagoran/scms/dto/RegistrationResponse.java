package com.janajagoran.scms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {
    private Long userId;
    private String name;
    private String email;
    private String membershipId;
    private String status;   // PENDING
    private String message;
}
