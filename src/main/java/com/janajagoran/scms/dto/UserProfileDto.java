package com.janajagoran.scms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long userId;
    private Long memberId;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String profilePicture;
    private String membershipId;
    private String address;
    private LocalDate dateOfJoining;
    private BigDecimal monthlyFee;
    private String status;
    private Boolean isActive;
}
