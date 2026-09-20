package com.janajagoran.scms.repository;

import com.janajagoran.scms.entity.Feedback;
import com.janajagoran.scms.enums.FeedbackStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByUserId(Long userId);
    List<Feedback> findByStatus(FeedbackStatus status);
}
