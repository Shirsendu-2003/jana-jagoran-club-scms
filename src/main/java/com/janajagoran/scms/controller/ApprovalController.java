package com.janajagoran.scms.controller;

import com.janajagoran.scms.dto.ApiResponse;
import com.janajagoran.scms.entity.ApprovalAction;
import com.janajagoran.scms.entity.ApprovalRequest;
import com.janajagoran.scms.enums.RoleName;
import com.janajagoran.scms.security.UserPrincipal;
import com.janajagoran.scms.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Read-only, unified view into the generic approval engine -- "what's pending, who requested it,
 * what's the full history of decisions on it". Deciding on a request still happens through the
 * domain-specific endpoint that knows how to apply the change (DesignationController, AdminController
 * member approve/reject, etc.) -- this controller is the inbox/audit view, not a generic decide-anything
 * endpoint, since only the domain service knows what "approved" should actually DO for each request type.
 */
@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SECRETARY','PRESIDENT','ADMIN','SUPER_ADMIN')")
public class ApprovalController {

    private final ApprovalService approvalService;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ApprovalRequest>>> pending() {
        return ResponseEntity.ok(ApiResponse.ok("Pending approvals fetched", approvalService.getAllPending()));
    }

    @GetMapping("/pending/mine")
    public ResponseEntity<ApiResponse<List<ApprovalRequest>>> pendingForMyRole(@AuthenticationPrincipal UserPrincipal principal) {
        RoleName role = RoleName.valueOf(principal.getRole().replace("ROLE_", ""));
        return ResponseEntity.ok(ApiResponse.ok("Pending approvals for your role fetched", approvalService.getPendingForRole(role)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ApprovalRequest>>> all(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok("Approval requests fetched", approvalService.getAll(PageRequest.of(page, size))));
    }

    @GetMapping("/{id}/actions")
    public ResponseEntity<ApiResponse<List<ApprovalAction>>> actionsForRequest(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Approval actions fetched", approvalService.getActionsForRequest(id)));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<List<ApprovalRequest>>> forEntity(@PathVariable String entityType, @PathVariable Long entityId) {
        return ResponseEntity.ok(ApiResponse.ok("Approval requests fetched", approvalService.getForEntity(entityType, entityId)));
    }
}
