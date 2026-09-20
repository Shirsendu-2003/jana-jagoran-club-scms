package com.janajagoran.scms.service;

import com.janajagoran.scms.dto.FeedbackRequest;
import com.janajagoran.scms.entity.Feedback;
import com.janajagoran.scms.entity.User;
import com.janajagoran.scms.enums.FeedbackStatus;
import com.janajagoran.scms.enums.FeedbackType;
import com.janajagoran.scms.exception.ResourceNotFoundException;
import com.janajagoran.scms.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    @Transactional
    public Feedback submit(FeedbackRequest request, User user) {
        FeedbackType type = FeedbackType.SUGGESTION;
        if (request.getType() != null) {
            try {
                type = FeedbackType.valueOf(request.getType().toUpperCase());
            } catch (IllegalArgumentException ignored) { }
        }
        Feedback feedback = Feedback.builder()
                .user(user)
                .type(type)
                .message(request.getMessage())
                .status(FeedbackStatus.OPEN)
                .build();
        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getMyFeedback(Long userId) {
        return feedbackRepository.findByUserId(userId);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }

    @Transactional
    public Feedback updateStatus(Long id, String status) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found"));
        feedback.setStatus(FeedbackStatus.valueOf(status.toUpperCase()));
        return feedbackRepository.save(feedback);
    }
}
