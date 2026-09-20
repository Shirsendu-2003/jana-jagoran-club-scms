package com.janajagoran.scms.service;

import com.janajagoran.scms.dto.MemberTimelineEventDto;
import com.janajagoran.scms.entity.AuditLog;
import com.janajagoran.scms.entity.Member;
import com.janajagoran.scms.entity.MemberDesignationHistory;
import com.janajagoran.scms.exception.ResourceNotFoundException;
import com.janajagoran.scms.repository.AuditLogRepository;
import com.janajagoran.scms.repository.MemberDesignationHistoryRepository;
import com.janajagoran.scms.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Builds a member's timeline purely by reading and merging audit_logs + member_desg_history --
 * never a manually maintained text blob, so it's always accurate and reflects new event types
 * automatically as they're added elsewhere in the system.
 */
@Service
@RequiredArgsConstructor
public class TimelineService {

    private final AuditLogRepository auditLogRepository;
    private final MemberDesignationHistoryRepository historyRepository;
    private final MemberRepository memberRepository;

    public List<MemberTimelineEventDto> getMemberTimeline(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        Long userId = member.getUser().getId();

        List<MemberTimelineEventDto> events = new ArrayList<>();

        // Registration / approval / activation / deactivation / password events -- from audit_logs
        Specification<AuditLog> spec = (root, query, cb) -> cb.equal(root.get("targetUserId"), userId);
        List<AuditLog> logs = auditLogRepository.findAll(spec);
        for (AuditLog log : logs) {
            events.add(MemberTimelineEventDto.builder()
                    .date(log.getCreatedAt())
                    .eventType(log.getAction().name())
                    .description(log.getDescription() != null ? log.getDescription() : log.getAction().name())
                    .performedBy(log.getPerformedBy() != null ? log.getPerformedBy().getName() : "System")
                    .build());
        }

        // Designation assign/release episodes -- from member_desg_history
        List<MemberDesignationHistory> history = historyRepository.findByMemberIdOrderByAssignmentDateDesc(memberId);
        for (MemberDesignationHistory h : history) {
            if (h.getAssignmentDate() != null) {
                events.add(MemberTimelineEventDto.builder()
                        .date(h.getAssignmentDate())
                        .eventType("DESIGNATION_" + h.getAction().name())
                        .description((h.getAction().name().equals("REJECTED") ? "Assignment to " : "Assigned as ")
                                + h.getDesignation().getTitle()
                                + (h.getAssignedByRole() != null ? " (requested by " + h.getAssignedByRole() + ")" : ""))
                        .performedBy(h.getApprovedBy() != null ? h.getApprovedBy().getName() : null)
                        .build());
            }
            if (h.getReleaseDate() != null && h.getAction().name().equals("RELEASED")) {
                events.add(MemberTimelineEventDto.builder()
                        .date(h.getReleaseDate())
                        .eventType("DESIGNATION_RELEASED")
                        .description("Released from " + h.getDesignation().getTitle()
                                + (h.getReason() != null ? (": " + h.getReason()) : ""))
                        .performedBy(h.getApprovedBy() != null ? h.getApprovedBy().getName() : null)
                        .build());
            }
        }

        events.sort(Comparator.comparing(MemberTimelineEventDto::getDate));
        return events;
    }
}
