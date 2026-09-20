package com.janajagoran.scms.controller;

import com.janajagoran.scms.dto.ApiResponse;
import com.janajagoran.scms.dto.PhotoSubmissionResponse;
import com.janajagoran.scms.entity.PhotoSubmission;
import com.janajagoran.scms.entity.User;
import com.janajagoran.scms.security.UserPrincipal;
import com.janajagoran.scms.service.PhotoSubmissionService;
import com.janajagoran.scms.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Public photo submission from the landing page -- open to anyone, logged in or not.
 * Submissions are held as PENDING and are never publicly visible until a
 * Secretary / President / Admin / Super Admin approves them.
 */
@RestController
@RequestMapping("/api/public/photos")
@RequiredArgsConstructor
public class PublicPhotoController {

    private final PhotoSubmissionService photoSubmissionService;
    private final UserService userService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submit(
            @RequestParam("file") MultipartFile file,
            @RequestParam("photographerName") String photographerName,
            @RequestParam(required = false) String caption,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String contactEmail,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request) {

        // Attribute to the account only if someone happens to be logged in; otherwise anonymous.
        User submittedBy = principal != null ? userService.getReference(principal.getId()) : null;

        PhotoSubmission saved = photoSubmissionService.submit(
                file, photographerName, caption, location, contactEmail, submittedBy, clientIp(request));

        return ResponseEntity.ok(ApiResponse.ok(
                "Thank you! Your photo has been submitted and will appear once a club moderator approves it.",
                Map.of("submissionId", saved.getId(), "status", saved.getStatus().name())));
    }

    /** Approved photos for the landing-page gallery strip. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PhotoSubmissionResponse>>> approvedPhotos(
            @RequestParam(defaultValue = "12") int limit) {
        return ResponseEntity.ok(ApiResponse.ok("Approved photos fetched", photoSubmissionService.getPublicFeed(limit)));
    }

    /** Respects X-Forwarded-For so rate limiting still works behind a reverse proxy. */
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
