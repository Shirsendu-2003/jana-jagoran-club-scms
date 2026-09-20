package com.janajagoran.scms.controller;

import com.janajagoran.scms.dto.*;
import com.janajagoran.scms.entity.*;
import com.janajagoran.scms.exception.ResourceNotFoundException;
import com.janajagoran.scms.repository.MemberRepository;
import com.janajagoran.scms.security.UserPrincipal;
import com.janajagoran.scms.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/** Endpoints available to Members (also inherited by higher roles for their own member-side actions). */
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final PaymentService paymentService;
    private final EventService eventService;
    private final NoticeService noticeService;
    private final GalleryService galleryService;
    private final FeedbackService feedbackService;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final com.janajagoran.scms.service.TimelineService timelineService;
    private final com.janajagoran.scms.service.DesignationService designationService;

    private Member currentMember(UserPrincipal principal) {
        return memberRepository.findByUserId(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Member profile not found for this account"));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard(@AuthenticationPrincipal UserPrincipal principal) {
        Member member = currentMember(principal);
        MemberPaymentSummaryDto summary = paymentService.getPendingSummary(member);

        Map<String, Object> data = Map.of(
                "membershipId", member.getMembershipId(),
                "status", member.getStatus().name(),
                "paymentSummary", summary,
                "upcomingEvents", eventService.getUpcomingEvents(),
                "recentNotices", noticeService.getAllNotices().stream().limit(5).toList(),
                "featuredGallery", galleryService.getFeaturedImages()
        );
        return ResponseEntity.ok(ApiResponse.ok("Dashboard data fetched", data));
    }

    // ---------------- Payments ----------------

    @GetMapping("/payments/history")
    public ResponseEntity<ApiResponse<List<Payment>>> paymentHistory(@AuthenticationPrincipal UserPrincipal principal) {
        Member member = currentMember(principal);
        return ResponseEntity.ok(ApiResponse.ok("Payment history fetched", paymentService.getPaymentHistory(member.getId())));
    }

    @GetMapping("/payments/summary")
    public ResponseEntity<ApiResponse<MemberPaymentSummaryDto>> paymentSummary(@AuthenticationPrincipal UserPrincipal principal) {
        Member member = currentMember(principal);
        return ResponseEntity.ok(ApiResponse.ok("Payment summary fetched", paymentService.getPendingSummary(member)));
    }

    // ---------------- Events ----------------

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<Event>>> events() {
        return ResponseEntity.ok(ApiResponse.ok("Events fetched", eventService.getAllEvents()));
    }

    @PostMapping("/events/{eventId}/register")
    public ResponseEntity<ApiResponse<EventRegistration>> registerForEvent(
            @PathVariable Long eventId, @AuthenticationPrincipal UserPrincipal principal) {
        EventRegistration reg = eventService.registerForEvent(eventId, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Registered for event successfully", reg));
    }

    // ---------------- Notices ----------------

    @GetMapping("/notices")
    public ResponseEntity<ApiResponse<List<Notice>>> notices() {
        return ResponseEntity.ok(ApiResponse.ok("Notices fetched", noticeService.getAllNotices()));
    }

    // ---------------- Gallery ----------------

    @GetMapping("/gallery/albums")
    public ResponseEntity<ApiResponse<List<Gallery>>> albums() {
        return ResponseEntity.ok(ApiResponse.ok("Albums fetched", galleryService.getAllAlbums()));
    }

    @GetMapping("/gallery/albums/{albumId}/images")
    public ResponseEntity<ApiResponse<List<GalleryImage>>> albumImages(@PathVariable Long albumId) {
        return ResponseEntity.ok(ApiResponse.ok("Images fetched", galleryService.getImagesForAlbum(albumId)));
    }

    @PostMapping(value = "/gallery/albums/{albumId}/upload", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<GalleryImage>> uploadPhoto(
            @PathVariable Long albumId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        String url = fileStorageService.store(file, "gallery");
        User user = userService.getReference(principal.getId());
        GalleryImage image = galleryService.uploadImage(albumId, url, user);
        return ResponseEntity.ok(ApiResponse.ok("Photo uploaded, pending approval", image));
    }

    @DeleteMapping("/gallery/images/{imageId}")
    public ResponseEntity<ApiResponse<Object>> deleteOwnPhoto(
            @PathVariable Long imageId, @AuthenticationPrincipal UserPrincipal principal) {
        galleryService.deleteImage(imageId, principal.getId(), false);
        return ResponseEntity.ok(ApiResponse.ok("Photo deleted"));
    }

    // ---------------- Feedback ----------------

    @PostMapping("/feedback")
    public ResponseEntity<ApiResponse<Feedback>> submitFeedback(
            @RequestBody FeedbackRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getReference(principal.getId());
        Feedback feedback = feedbackService.submit(request, user);
        return ResponseEntity.ok(ApiResponse.ok("Feedback submitted, thank you!", feedback));
    }

    @GetMapping("/feedback/mine")
    public ResponseEntity<ApiResponse<List<Feedback>>> myFeedback(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Feedback fetched", feedbackService.getMyFeedback(principal.getId())));
    }

    // ---------------- Notifications ----------------

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<Notification>>> notifications(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok("Notifications fetched", notificationService.getMyNotifications(principal.getId())));
    }

    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<ApiResponse<Object>> markRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.ok("Marked as read"));
    }

    // ---------------- My History / My Designation ----------------

    /** "My History" on the Member dashboard -- a member's own timeline, no elevated role needed. */
    @GetMapping("/my-timeline")
    public ResponseEntity<ApiResponse<List<com.janajagoran.scms.dto.MemberTimelineEventDto>>> myTimeline(
            @AuthenticationPrincipal UserPrincipal principal) {
        Member member = currentMember(principal);
        return ResponseEntity.ok(ApiResponse.ok("Your timeline fetched", timelineService.getMemberTimeline(member.getId())));
    }

    /** "My Designation" -- the member's own current + past designation episodes with durations. */
    @GetMapping("/my-designations")
    public ResponseEntity<ApiResponse<List<com.janajagoran.scms.dto.AssignmentHistoryDto>>> myDesignations(
            @AuthenticationPrincipal UserPrincipal principal) {
        Member member = currentMember(principal);
        return ResponseEntity.ok(ApiResponse.ok("Your designations fetched", designationService.getAssignmentHistoryForMember(member.getId())));
    }
}
