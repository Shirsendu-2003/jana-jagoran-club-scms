package com.janajagoran.scms.controller;

import com.janajagoran.scms.dto.ApiResponse;
import com.janajagoran.scms.dto.HomepageContentRequest;
import com.janajagoran.scms.entity.HomepageContent;
import com.janajagoran.scms.entity.User;
import com.janajagoran.scms.security.UserPrincipal;
import com.janajagoran.scms.service.HomepageService;
import com.janajagoran.scms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Homepage CMS. Editing is restricted to Admin/Super Admin for now -- extending this to
 * "designated person" (a member holding a designation with HOMEPAGE_EDIT permission) is a
 * straightforward follow-up: check DesignationService.getMenuPermissions() for the member's
 * active designation and allow the write if HOMEPAGE_EDIT is present.
 */
@RestController
@RequestMapping("/api/admin/homepage")
@RequiredArgsConstructor
public class HomepageController {

    private final HomepageService homepageService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HomepageContent>>> allSections() {
        return ResponseEntity.ok(ApiResponse.ok("Homepage sections fetched", homepageService.getAllSections()));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<HomepageContent>> upsertSection(
            @Valid @RequestBody HomepageContentRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getReference(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Homepage section saved", homepageService.upsertSection(request, user)));
    }
}
