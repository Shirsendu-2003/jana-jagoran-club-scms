package com.janajagoran.scms.controller;

import com.janajagoran.scms.dto.*;
import com.janajagoran.scms.entity.*;
import com.janajagoran.scms.enums.MenuKey;
import com.janajagoran.scms.security.UserPrincipal;
import com.janajagoran.scms.service.DesignationService;
import com.janajagoran.scms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Designation ("desg_master" / "desg_post_details") management.
 * Proposing a designation or requesting an assignment/release is open to any staff role
 * (Secretary, President, Admin) -- the "operator" from the diagram; deciding on those
 * requests (approve/reject) and menu allotment are Admin-only actions.
 */
@RestController
@RequestMapping("/api/designations")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationService designationService;
    private final UserService userService;

    // ---------------- Designation catalog & creation approval ----------------

    @PreAuthorize("hasAnyRole('SECRETARY','PRESIDENT','ADMIN','SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DesignationMaster>>> allDesignations() {
        return ResponseEntity.ok(ApiResponse.ok("Designations fetched", designationService.getAllDesignations()));
    }

    @PreAuthorize("hasAnyRole('SECRETARY','PRESIDENT','ADMIN','SUPER_ADMIN')")
    @GetMapping("/{id}/post-detail")
    public ResponseEntity<ApiResponse<DesignationPostDetail>> postDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Post detail fetched", designationService.getPostDetail(id)));
    }

    @PreAuthorize("hasAnyRole('SECRETARY','PRESIDENT','ADMIN','SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<DesignationMaster>> propose(
            @Valid @RequestBody DesignationRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        User operator = userService.getReference(principal.getId());
        DesignationMaster designation = designationService.proposeDesignation(request, operator);
        return ResponseEntity.ok(ApiResponse.ok("Designation proposed -- awaiting Admin approval", designation));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PatchMapping("/{id}/decide")
    public ResponseEntity<ApiResponse<DesignationPostDetail>> decide(
            @PathVariable Long id, @RequestParam boolean approve, @RequestParam(required = false) String remarks,
            @AuthenticationPrincipal UserPrincipal principal) {
        User admin = userService.getReference(principal.getId());
        DesignationPostDetail result = designationService.decideDesignation(id, approve, admin, remarks);
        return ResponseEntity.ok(ApiResponse.ok(approve ? "Designation approved and now active" : "Designation rejected", result));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PatchMapping("/{id}/retire")
    public ResponseEntity<ApiResponse<DesignationPostDetail>> retire(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        User admin = userService.getReference(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Designation retired", designationService.retireDesignation(id, admin)));
    }

    // ---------------- Menu Allotment ----------------

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PutMapping("/{id}/menu-permissions")
    public ResponseEntity<ApiResponse<List<DesignationPermission>>> setMenuPermissions(
            @PathVariable Long id, @RequestBody MenuPermissionRequest request) {
        List<MenuKey> keys = request.getMenuKeys().stream().map(MenuKey::valueOf).toList();
        return ResponseEntity.ok(ApiResponse.ok("Menu permissions updated", designationService.setMenuPermissions(id, keys)));
    }

    @PreAuthorize("hasAnyRole('SECRETARY','PRESIDENT','ADMIN','SUPER_ADMIN')")
    @GetMapping("/{id}/menu-permissions")
    public ResponseEntity<ApiResponse<List<DesignationPermission>>> getMenuPermissions(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Menu permissions fetched", designationService.getMenuPermissions(id)));
    }

    // ---------------- Assign / Release ----------------

    @PreAuthorize("hasAnyRole('SECRETARY','PRESIDENT','ADMIN','SUPER_ADMIN')")
    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<MemberDesignationDetail>> requestAssignment(
            @Valid @RequestBody AssignDesignationRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        User requestedBy = userService.getReference(principal.getId());
        MemberDesignationDetail detail = designationService.requestAssignment(request.getMemberId(), request.getDesignationId(), requestedBy);
        return ResponseEntity.ok(ApiResponse.ok("Assignment requested -- awaiting approval", detail));
    }

    @PreAuthorize("hasAnyRole('PRESIDENT','ADMIN','SUPER_ADMIN')")
    @PatchMapping("/assignments/{id}/decide")
    public ResponseEntity<ApiResponse<MemberDesignationDetail>> decideAssignment(
            @PathVariable Long id, @RequestParam boolean approve, @RequestParam(required = false) String remarks,
            @AuthenticationPrincipal UserPrincipal principal) {
        User approver = userService.getReference(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(approve ? "Assignment approved" : "Assignment rejected",
                designationService.decideAssignment(id, approve, approver, remarks)));
    }

    @PreAuthorize("hasAnyRole('SECRETARY','PRESIDENT','ADMIN','SUPER_ADMIN')")
    @PostMapping("/assignments/{id}/request-release")
    public ResponseEntity<ApiResponse<MemberDesignationDetail>> requestRelease(
            @PathVariable Long id, @Valid @RequestBody ReleaseDesignationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        User requestedBy = userService.getReference(principal.getId());
        MemberDesignationDetail detail = designationService.requestRelease(id, request.getReason(), requestedBy);
        return ResponseEntity.ok(ApiResponse.ok("Release requested -- awaiting approval", detail));
    }

    @PreAuthorize("hasAnyRole('PRESIDENT','ADMIN','SUPER_ADMIN')")
    @PatchMapping("/assignments/{id}/decide-release")
    public ResponseEntity<ApiResponse<Object>> decideRelease(
            @PathVariable Long id, @RequestParam boolean approve, @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserPrincipal principal) {
        User approver = userService.getReference(principal.getId());
        designationService.decideRelease(id, approve, reason, approver);
        return ResponseEntity.ok(ApiResponse.ok(approve ? "Member released from designation" : "Release request dismissed"));
    }

    @PreAuthorize("hasAnyRole('SECRETARY','PRESIDENT','ADMIN','SUPER_ADMIN')")
    @GetMapping("/assignments/pending")
    public ResponseEntity<ApiResponse<List<MemberDesignationDetail>>> pendingAssignments() {
        return ResponseEntity.ok(ApiResponse.ok("Pending assignments fetched", designationService.getPendingAssignments()));
    }

    @PreAuthorize("hasAnyRole('SECRETARY','PRESIDENT','ADMIN','SUPER_ADMIN')")
    @GetMapping("/assignments/active")
    public ResponseEntity<ApiResponse<List<MemberDesignationDetail>>> activeAssignments() {
        return ResponseEntity.ok(ApiResponse.ok("Active assignments fetched", designationService.getActiveAssignments()));
    }

    @PreAuthorize("hasAnyRole('SECRETARY','PRESIDENT','ADMIN','SUPER_ADMIN')")
    @GetMapping("/members/{memberId}")
    public ResponseEntity<ApiResponse<List<MemberDesignationDetail>>> memberDesignations(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.ok("Member designations fetched", designationService.getMemberDesignations(memberId)));
    }

    @PreAuthorize("hasAnyRole('SECRETARY','PRESIDENT','ADMIN','SUPER_ADMIN')")
    @GetMapping("/members/{memberId}/history")
    public ResponseEntity<ApiResponse<List<MemberDesignationHistory>>> memberHistory(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.ok("Designation history fetched", designationService.getMemberHistory(memberId)));
    }

    /** Assignment History Dashboard: who was assigned what, by whom, approved by whom, start/end, duration, status. */
    @PreAuthorize("hasAnyRole('SECRETARY','PRESIDENT','ADMIN','SUPER_ADMIN')")
    @GetMapping("/assignment-history")
    public ResponseEntity<ApiResponse<List<com.janajagoran.scms.dto.AssignmentHistoryDto>>> assignmentHistory() {
        return ResponseEntity.ok(ApiResponse.ok("Assignment history fetched", designationService.getAssignmentHistoryDtos()));
    }

    @PreAuthorize("hasAnyRole('SECRETARY','PRESIDENT','ADMIN','SUPER_ADMIN')")
    @GetMapping("/assignment-history/{memberId}")
    public ResponseEntity<ApiResponse<List<com.janajagoran.scms.dto.AssignmentHistoryDto>>> assignmentHistoryForMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.ok("Assignment history fetched", designationService.getAssignmentHistoryForMember(memberId)));
    }
}
