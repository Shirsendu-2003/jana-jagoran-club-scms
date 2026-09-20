package com.janajagoran.scms.controller;

import com.janajagoran.scms.dto.*;
import com.janajagoran.scms.security.UserPrincipal;
import com.janajagoran.scms.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegistrationResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegistrationResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok(response.getMessage(), response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Object>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully"));
    }

    /** Called right after login when the response's mustChangePassword flag is true
     *  (temp password issued by an Admin, or another forced-reset scenario). No current
     *  password is required -- the still-valid JWT from the temp-password login proves identity. */
    @PostMapping("/first-login-password")
    public ResponseEntity<ApiResponse<Object>> firstLoginSetPassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FirstLoginPasswordRequest request) {
        authService.firstLoginSetPassword(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Password updated. You're all set."));
    }

    /** Step 1 of OTP-verified password change: sends a 6-digit code via in-app notification. */
    @PostMapping("/password-otp/request")
    public ResponseEntity<ApiResponse<OtpRequestResponse>> requestPasswordOtp(@AuthenticationPrincipal UserPrincipal principal) {
        OtpRequestResponse response = authService.requestPasswordChangeOtp(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response.getMessage(), response));
    }

    /** Step 2: confirm the OTP and set the new password. */
    @PostMapping("/password-otp/confirm")
    public ResponseEntity<ApiResponse<Object>> confirmPasswordOtp(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody OtpConfirmRequest request) {
        authService.confirmPasswordChangeOtp(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully via OTP verification"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String token = authService.forgotPassword(request);
        // token returned only because SMTP may not be configured in this demo setup;
        // in production, remove `data` and email the reset link instead.
        return ResponseEntity.ok(ApiResponse.ok("Password reset instructions generated", token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout() {
        // Stateless JWT: logout is handled client-side by discarding the token.
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully"));
    }
}
