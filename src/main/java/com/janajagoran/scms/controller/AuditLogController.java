package com.janajagoran.scms.controller;

import com.janajagoran.scms.dto.ApiResponse;
import com.janajagoran.scms.entity.AuditLog;
import com.janajagoran.scms.enums.AuditAction;
import com.janajagoran.scms.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * /admin/audit-logs and /super-admin/audit-logs both point here -- Admin and Super Admin
 * share full read access to the audit trail. Nobody -- not even Super Admin -- can delete
 * entries; there is deliberately no DELETE endpoint on this controller.
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLog>>> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long performedByUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        AuditAction actionEnum = null;
        if (action != null && !action.isBlank()) {
            try {
                actionEnum = AuditAction.valueOf(action.toUpperCase());
            } catch (IllegalArgumentException ignored) { /* leave null -- unfiltered on action */ }
        }

        Page<AuditLog> result = auditLogService.search(from, to, actionEnum, entityType, performedByUserId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok("Audit logs fetched", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLog>> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Audit log fetched", auditLogService.getById(id)));
    }
}
