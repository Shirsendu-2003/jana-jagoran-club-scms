package com.janajagoran.scms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Returned to the Admin after resetting a user's password, since no SMS/email gateway is wired in --
 *  the Admin is expected to relay this to the user through a trusted channel (phone call, in person, etc). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminResetPasswordResponse {
    private Long userId;
    private String email;
    private String temporaryPassword;
    private String message;
}
