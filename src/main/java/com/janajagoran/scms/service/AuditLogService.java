package com.janajagoran.scms.service;

import com.janajagoran.scms.entity.AuditLog;
import com.janajagoran.scms.entity.User;
import com.janajagoran.scms.enums.AuditAction;
import com.janajagoran.scms.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * Centralized audit logging -- see AuditLog entity. Automatically captures IP address and
 * User-Agent from the current HTTP request when available, so call sites don't need to
 * thread HttpServletRequest through every service method.
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /** Full-detail entry point -- use when you have old/new values and a target user to record. */
    public void log(AuditAction action, User performedBy, String entityType, Long entityId,
                     String description, String oldValue, String newValue,
                     Long targetUserId, String targetUserName) {
        HttpServletRequest request = currentRequest();

        AuditLog log = AuditLog.builder()
                .action(action)
                .performedBy(performedBy)
                .performedByRole(performedBy != null && performedBy.getRole() != null ? performedBy.getRole().getName() : null)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .oldValue(oldValue)
                .newValue(newValue)
                .targetUserId(targetUserId)
                .targetUserName(targetUserName)
                .ipAddress(request != null ? request.getRemoteAddr() : null)
                .userAgent(request != null ? request.getHeader("User-Agent") : null)
                .build();
        auditLogRepository.save(log);
    }

    /** Convenience overload for simple entries without old/new value tracking. */
    public void log(User performedBy, AuditAction action, String entityType, Long entityId, String description) {
        log(action, performedBy, entityType, entityId, description, null, null, null, null);
    }

    private HttpServletRequest currentRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public Page<AuditLog> getRecentLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public AuditLog getById(Long id) {
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new com.janajagoran.scms.exception.ResourceNotFoundException("Audit log entry not found"));
    }

    /** Filtered search for the Audit Log Dashboard: date range, role, action, entity type, performer. */
    public Page<AuditLog> search(LocalDateTime from, LocalDateTime to, AuditAction action,
                                  String entityType, Long performedByUserId, Pageable pageable) {
        Specification<AuditLog> spec = Specification.where(null);

        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }
        if (action != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("action"), action));
        }
        if (entityType != null && !entityType.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("entityType"), entityType));
        }
        if (performedByUserId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("performedBy").get("id"), performedByUserId));
        }

        return auditLogRepository.findAll(spec, pageable);
    }
}
