package com.janajagoran.scms.controller;

import com.janajagoran.scms.dto.*;
import com.janajagoran.scms.entity.*;
import com.janajagoran.scms.enums.MemberStatus;
import com.janajagoran.scms.security.UserPrincipal;
import com.janajagoran.scms.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Endpoints for the Admin role: user management, member management, notices, events, gallery approvals. */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final NoticeService noticeService;
    private final EventService eventService;
    private final GalleryService galleryService;
    private final DashboardService dashboardService;
    private final FileStorageService fileStorageService;
    private final FeedbackService feedbackService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> dashboard() {
        return ResponseEntity.ok(ApiResponse.ok("Dashboard fetched", dashboardService.getStats()));
    }

    // ---------------- User Management ----------------

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User created", userService.createUser(request)));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> allUsers() {
        return ResponseEntity.ok(ApiResponse.ok("Users fetched", userService.getAllUsers()));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<User>> updateRole(@PathVariable Long id, @RequestParam String role) {
        return ResponseEntity.ok(ApiResponse.ok("Role updated", userService.updateUserRole(id, role)));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<User>> setStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(ApiResponse.ok(active ? "User activated" : "User deactivated", userService.setActiveStatus(id, active)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted"));
    }

    /** "Admin Profile Edit(Search User ---> Password Reset ---> User Id ---> Password)" */
    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<ApiResponse<AdminResetPasswordResponse>> resetUserPassword(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        User admin = userService.getReference(principal.getId());
        AdminResetPasswordResponse response = userService.resetUserPassword(id, admin);
        return ResponseEntity.ok(ApiResponse.ok(response.getMessage(), response));
    }

    // ---------------- Member Management ----------------

    @GetMapping("/members/search")
    public ResponseEntity<ApiResponse<List<Member>>> searchMembers(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.ok("Members fetched", userService.searchMembers(keyword)));
    }

    @GetMapping("/members/pending")
    public ResponseEntity<ApiResponse<List<Member>>> pendingMembers() {
        return ResponseEntity.ok(ApiResponse.ok("Pending registrations fetched", userService.getPendingMembers()));
    }

    @PostMapping("/members/{id}/approve")
    public ResponseEntity<ApiResponse<Member>> approveMember(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        User admin = userService.getReference(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Member approved -- they can now log in", userService.approveMember(id, admin)));
    }

    @PostMapping("/members/{id}/reject")
    public ResponseEntity<ApiResponse<Member>> rejectMember(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        User admin = userService.getReference(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Member registration rejected", userService.rejectMember(id, admin)));
    }

    @PatchMapping("/members/{id}/status")
    public ResponseEntity<ApiResponse<Member>> setMemberStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.ok("Member status updated",
                userService.setMemberStatus(id, MemberStatus.valueOf(status.toUpperCase()))));
    }

    /** "Member Active/Inactive -> table -> switch -> modal reason -> save" */
    @PatchMapping("/members/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Member>> toggleMemberStatus(
            @PathVariable Long id, @Valid @RequestBody SetActiveStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        User performedBy = userService.getReference(principal.getId());
        Member member = userService.toggleMemberActiveStatus(id, request.isActive(), request.getReason(), performedBy);
        return ResponseEntity.ok(ApiResponse.ok(request.isActive() ? "Member activated" : "Member deactivated", member));
    }

    // ---------------- Notice Management ----------------

    @PostMapping("/notices")
    public ResponseEntity<ApiResponse<Notice>> createNotice(
            @Valid @RequestBody NoticeRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getReference(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Notice published", noticeService.createNotice(request, user)));
    }

    @PutMapping("/notices/{id}")
    public ResponseEntity<ApiResponse<Notice>> updateNotice(@PathVariable Long id, @Valid @RequestBody NoticeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Notice updated", noticeService.updateNotice(id, request)));
    }

    @DeleteMapping("/notices/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.ok(ApiResponse.ok("Notice deleted"));
    }

    // ---------------- Event Management ----------------

    @PostMapping("/events")
    public ResponseEntity<ApiResponse<Event>> createEvent(
            @Valid @RequestBody EventRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getReference(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Event created", eventService.createEvent(request, user)));
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<ApiResponse<Event>> updateEvent(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Event updated", eventService.updateEvent(id, request)));
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.ok("Event deleted"));
    }

    // ---------------- Gallery Management ----------------

    @GetMapping("/gallery/pending")
    public ResponseEntity<ApiResponse<List<GalleryImage>>> pendingImages() {
        return ResponseEntity.ok(ApiResponse.ok("Pending images fetched", galleryService.getPendingImages()));
    }

    @GetMapping("/gallery/approved")
    public ResponseEntity<ApiResponse<List<GalleryImage>>> approvedImages() {
        return ResponseEntity.ok(ApiResponse.ok("Approved images fetched", galleryService.getApprovedImages()));
    }

    @PatchMapping("/gallery/{imageId}/approve")
    public ResponseEntity<ApiResponse<GalleryImage>> approveImage(@PathVariable Long imageId, @RequestParam boolean approve) {
        return ResponseEntity.ok(ApiResponse.ok("Image status updated", galleryService.approveImage(imageId, approve)));
    }

    @PatchMapping("/gallery/{imageId}/featured")
    public ResponseEntity<ApiResponse<GalleryImage>> setFeatured(@PathVariable Long imageId, @RequestParam boolean featured) {
        return ResponseEntity.ok(ApiResponse.ok("Featured status updated", galleryService.setFeatured(imageId, featured)));
    }

    @DeleteMapping("/gallery/{imageId}")
    public ResponseEntity<ApiResponse<Object>> deleteImage(@PathVariable Long imageId, @AuthenticationPrincipal UserPrincipal principal) {
        galleryService.deleteImage(imageId, principal.getId(), true);
        return ResponseEntity.ok(ApiResponse.ok("Image deleted"));
    }

    // ---------------- Feedback ----------------

    @GetMapping("/feedback")
    public ResponseEntity<ApiResponse<List<Feedback>>> allFeedback() {
        return ResponseEntity.ok(ApiResponse.ok("Feedback fetched", feedbackService.getAllFeedback()));
    }

    @PatchMapping("/feedback/{id}/status")
    public ResponseEntity<ApiResponse<Feedback>> updateFeedbackStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.ok("Feedback status updated", feedbackService.updateStatus(id, status)));
    }
}
