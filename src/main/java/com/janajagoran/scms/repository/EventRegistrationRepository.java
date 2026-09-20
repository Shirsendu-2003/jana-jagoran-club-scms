package com.janajagoran.scms.repository;

import com.janajagoran.scms.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    List<EventRegistration> findByEventId(Long eventId);
    List<EventRegistration> findByMemberId(Long memberId);
    Optional<EventRegistration> findByEventIdAndMemberId(Long eventId, Long memberId);
    long countByEventId(Long eventId);
}
