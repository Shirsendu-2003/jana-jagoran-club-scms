package com.janajagoran.scms.controller;

import com.janajagoran.scms.dto.ApiResponse;
import com.janajagoran.scms.dto.PublicStatsDto;
import com.janajagoran.scms.entity.Event;
import com.janajagoran.scms.entity.Faq;
import com.janajagoran.scms.entity.GalleryImage;
import com.janajagoran.scms.entity.HomepageContent;
import com.janajagoran.scms.entity.Notice;
import com.janajagoran.scms.enums.MemberStatus;
import com.janajagoran.scms.repository.MemberRepository;
import com.janajagoran.scms.service.EventService;
import com.janajagoran.scms.service.FaqService;
import com.janajagoran.scms.service.GalleryService;
import com.janajagoran.scms.service.HomepageService;
import com.janajagoran.scms.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public, unauthenticated endpoints for the marketing/landing page.
 * Only read-only, non-sensitive data is exposed here -- gallery photos are
 * filtered to APPROVED + featured only, so nothing pending/rejected can leak out.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final EventService eventService;
    private final NoticeService noticeService;
    private final GalleryService galleryService;
    private final MemberRepository memberRepository;
    private final HomepageService homepageService;
    private final FaqService faqService;

    @GetMapping("/events/upcoming")
    public ResponseEntity<ApiResponse<List<Event>>> upcomingEvents() {
        List<Event> events = eventService.getUpcomingEvents();
        List<Event> limited = events.size() > 3 ? events.subList(0, 3) : events;
        return ResponseEntity.ok(ApiResponse.ok("Upcoming events fetched", limited));
    }

    @GetMapping("/events/past")
    public ResponseEntity<ApiResponse<List<Event>>> pastEvents() {
        List<Event> events = eventService.getPastEvents();
        List<Event> limited = events.size() > 6 ? events.subList(0, 6) : events;
        return ResponseEntity.ok(ApiResponse.ok("Past events fetched", limited));
    }

    @GetMapping("/notices/latest")
    public ResponseEntity<ApiResponse<List<Notice>>> latestNotices() {
        List<Notice> notices = noticeService.getAllNotices();
        List<Notice> limited = notices.size() > 3 ? notices.subList(0, 3) : notices;
        return ResponseEntity.ok(ApiResponse.ok("Latest notices fetched", limited));
    }

    @GetMapping("/gallery/showcase")
    public ResponseEntity<ApiResponse<List<GalleryImage>>> galleryShowcase() {
        List<GalleryImage> images = galleryService.getPublicShowcase();
        List<GalleryImage> limited = images.size() > 8 ? images.subList(0, 8) : images;
        return ResponseEntity.ok(ApiResponse.ok("Featured gallery photos fetched", limited));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<PublicStatsDto>> stats() {
        PublicStatsDto stats = PublicStatsDto.builder()
                .activeMembers(memberRepository.countByStatus(MemberStatus.ACTIVE))
                .totalEvents(eventService.getPastEvents().size() + eventService.getUpcomingEvents().size())
                .photosShared(galleryService.countApprovedImages())
                .build();
        return ResponseEntity.ok(ApiResponse.ok("Public stats fetched", stats));
    }

    @GetMapping("/homepage")
    public ResponseEntity<ApiResponse<List<HomepageContent>>> homepage() {
        return ResponseEntity.ok(ApiResponse.ok("Homepage content fetched", homepageService.getAllSections()));
    }

    @GetMapping("/faqs")
    public ResponseEntity<ApiResponse<List<Faq>>> faqs() {
        return ResponseEntity.ok(ApiResponse.ok("FAQs fetched", faqService.getPublicFaqs()));
    }
}
