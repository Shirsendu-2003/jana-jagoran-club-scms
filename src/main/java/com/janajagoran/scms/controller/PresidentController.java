package com.janajagoran.scms.controller;

import com.janajagoran.scms.dto.ApiResponse;
import com.janajagoran.scms.dto.DashboardStatsDto;
import com.janajagoran.scms.entity.Budget;
import com.janajagoran.scms.entity.Gallery;
import com.janajagoran.scms.entity.Notice;
import com.janajagoran.scms.security.UserPrincipal;
import com.janajagoran.scms.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints for the President role: financial oversight, approvals, member statistics. */
@RestController
@RequestMapping("/api/president")
@RequiredArgsConstructor
public class PresidentController {

    private final DashboardService dashboardService;
    private final BudgetService budgetService;
    private final UserService userService;
    private final NoticeService noticeService;
    private final GalleryService galleryService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> dashboard() {
        return ResponseEntity.ok(ApiResponse.ok("Dashboard fetched", dashboardService.getStats()));
    }

    @GetMapping("/budgets")
    public ResponseEntity<ApiResponse<List<Budget>>> budgets() {
        return ResponseEntity.ok(ApiResponse.ok("Budgets fetched", budgetService.getAllBudgets()));
    }

    @PatchMapping("/budgets/{id}/approve")
    public ResponseEntity<ApiResponse<Budget>> approveBudget(
            @PathVariable Long id, @RequestParam boolean approve, @AuthenticationPrincipal UserPrincipal principal) {
        var user = userService.getReference(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Budget decision recorded", budgetService.approveBudget(id, user, approve)));
    }

    @GetMapping("/notices")
    public ResponseEntity<ApiResponse<List<Notice>>> notices() {
        return ResponseEntity.ok(ApiResponse.ok("Notices fetched", noticeService.getAllNotices()));
    }

    @GetMapping("/gallery/albums")
    public ResponseEntity<ApiResponse<List<Gallery>>> albums() {
        return ResponseEntity.ok(ApiResponse.ok("Albums fetched", galleryService.getAllAlbums()));
    }
}
