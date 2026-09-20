package com.janajagoran.scms.controller;

import com.janajagoran.scms.dto.ApiResponse;
import com.janajagoran.scms.dto.DashboardStatsDto;
import com.janajagoran.scms.entity.AuditLog;
import com.janajagoran.scms.entity.User;
import com.janajagoran.scms.service.AuditLogService;
import com.janajagoran.scms.service.DashboardService;
import com.janajagoran.scms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints exclusive to the Super Admin role: complete system oversight,
 * admin/secretary/president account management, audit logs, financial analytics.
 * (Backup/restore and system-settings endpoints are stubbed as extension points --
 *  see README for recommended tools: mysqldump for backups, a settings table for config.)
 */
@RestController
@RequestMapping("/api/super-admin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final UserService userService;
    private final DashboardService dashboardService;
    private final AuditLogService auditLogService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> dashboard() {
        return ResponseEntity.ok(ApiResponse.ok("System overview fetched", dashboardService.getStats()));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> allUsers() {
        return ResponseEntity.ok(ApiResponse.ok("Users fetched", userService.getAllUsers()));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<User>> assignRole(@PathVariable Long id, @RequestParam String role) {
        return ResponseEntity.ok(ApiResponse.ok("Role assigned", userService.updateUserRole(id, role)));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<User>> setStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(ApiResponse.ok(active ? "Account activated" : "Account deactivated",
                userService.setActiveStatus(id, active)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted"));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> auditLogs(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.ok("Audit logs fetched",
                auditLogService.getRecentLogs(PageRequest.of(page, size))));
    }
}
