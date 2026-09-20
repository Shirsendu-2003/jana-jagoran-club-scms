package com.janajagoran.scms.controller;

import com.janajagoran.scms.dto.ApiResponse;
import com.janajagoran.scms.entity.PhotoSubmission;
import com.janajagoran.scms.entity.User;
import com.janajagoran.scms.security.UserPrincipal;
import com.janajagoran.scms.service.PhotoSubmissionService;
import com.janajagoran.scms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Moderation queue for public photo submissions. Per the requirement, Secretary,
 * President, Admin and Super Admin can all approve/reject -- any of them is enough,
 * a submission needs only one approval to go live.
 */
@RestController
@RequestMapping("/api/photo-submissions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SECRETARY','PRESIDENT','ADMIN','SUPER_ADMIN')")
public class PhotoSubmissionController {

    private final PhotoSubmissionService photoSubmissionService;
    private final UserService userService;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<PhotoSubmission>>> pending() {
        return ResponseEntity.ok(ApiResponse.ok("Pending photo submissions fetched", photoSubmissionService.getPending()));
    }

    @GetMapping("/approved")
    public ResponseEntity<ApiResponse<List<PhotoSubmission>>> approved() {
        return ResponseEntity.ok(ApiResponse.ok("Approved photo submissions fetched", photoSubmissionService.getApproved()));
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<ApiResponse<PhotoSubmission>> review(
            @PathVariable Long id,
            @RequestParam boolean approve,
            @RequestParam(required = false) String rejectionReason,
            @AuthenticationPrincipal UserPrincipal principal) {
        User reviewer = userService.getReference(principal.getId());
        PhotoSubmission result = photoSubmissionService.review(id, approve, rejectionReason, reviewer);
        return ResponseEntity.ok(ApiResponse.ok(
                approve ? "Photo approved -- it's now live on the home page" : "Photo rejected", result));
    }

    @PatchMapping("/{id}/featured")
    public ResponseEntity<ApiResponse<PhotoSubmission>> setFeatured(@PathVariable Long id, @RequestParam boolean featured) {
        return ResponseEntity.ok(ApiResponse.ok("Featured status updated", photoSubmissionService.setFeatured(id, featured)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        photoSubmissionService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Photo submission deleted"));
    }
}
