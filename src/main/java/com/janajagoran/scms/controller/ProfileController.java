package com.janajagoran.scms.controller;

import com.janajagoran.scms.dto.ApiResponse;
import com.janajagoran.scms.dto.UpdateProfileRequest;
import com.janajagoran.scms.dto.UserProfileDto;
import com.janajagoran.scms.security.UserPrincipal;
import com.janajagoran.scms.service.FileStorageService;
import com.janajagoran.scms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Shared profile endpoints available to every authenticated role. */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Profile fetched", userService.getProfile(principal.getId())));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<Object>> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateProfileRequest request) {
        userService.updateProfile(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated"));
    }

    @PostMapping(value = "/me/picture", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<String>> uploadProfilePicture(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file) {
        String url = fileStorageService.store(file, "profile-pictures");
        userService.updateProfilePicture(principal.getId(), url);
        return ResponseEntity.ok(ApiResponse.ok("Profile picture updated", url));
    }
}
