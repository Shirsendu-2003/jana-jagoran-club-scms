package com.janajagoran.scms.service;

import com.janajagoran.scms.entity.Notification;
import com.janajagoran.scms.entity.User;
import com.janajagoran.scms.enums.NotificationType;
import com.janajagoran.scms.repository.NotificationRepository;
import com.janajagoran.scms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public void notifyUser(User user, String title, String message, NotificationType type) {
        Notification n = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .build();
        notificationRepository.save(n);
    }

    public void notifyAllMembers(String title, String message, NotificationType type) {
        userRepository.findByRoleName(com.janajagoran.scms.enums.RoleName.MEMBER)
                .forEach(u -> notifyUser(u, title, message, type));
    }

    public List<Notification> getMyNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }
}
