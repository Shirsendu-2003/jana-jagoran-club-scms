package com.janajagoran.scms.controller;

import com.janajagoran.scms.dto.ApiResponse;
import com.janajagoran.scms.dto.MemberTimelineEventDto;
import com.janajagoran.scms.service.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * GET /api/members/{id}/timeline -- built purely from audit_logs + member_desg_history,
 * never a manually maintained text blob (see TimelineService).
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SECRETARY','PRESIDENT','ADMIN','SUPER_ADMIN')")
public class MemberTimelineController {

    private final TimelineService timelineService;

    @GetMapping("/{id}/timeline")
    public ResponseEntity<ApiResponse<List<MemberTimelineEventDto>>> timeline(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Member timeline fetched", timelineService.getMemberTimeline(id)));
    }
}
