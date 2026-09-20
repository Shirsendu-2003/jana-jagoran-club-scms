package com.janajagoran.scms.service;

import com.janajagoran.scms.dto.NoticeRequest;
import com.janajagoran.scms.entity.Notice;
import com.janajagoran.scms.entity.User;
import com.janajagoran.scms.enums.NoticeType;
import com.janajagoran.scms.enums.NotificationType;
import com.janajagoran.scms.exception.BadRequestException;
import com.janajagoran.scms.exception.ResourceNotFoundException;
import com.janajagoran.scms.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NotificationService notificationService;

    @Transactional
    public Notice createNotice(NoticeRequest request, User createdBy) {
        NoticeType type = NoticeType.GENERAL;
        if (request.getType() != null) {
            try {
                type = NoticeType.valueOf(request.getType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid notice type");
            }
        }

        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .type(type)
                .attachmentUrl(request.getAttachmentUrl())
                .createdBy(createdBy)
                .build();
        notice = noticeRepository.save(notice);

        notificationService.notifyAllMembers("New Notice: " + notice.getTitle(),
                notice.getContent().length() > 100 ? notice.getContent().substring(0, 100) + "..." : notice.getContent(),
                NotificationType.NOTICE);

        return notice;
    }

    @Transactional
    public Notice updateNotice(Long id, NoticeRequest request) {
        Notice notice = getNotice(id);
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        if (request.getType() != null) notice.setType(NoticeType.valueOf(request.getType().toUpperCase()));
        if (request.getAttachmentUrl() != null) notice.setAttachmentUrl(request.getAttachmentUrl());
        return noticeRepository.save(notice);
    }

    @Transactional
    public void deleteNotice(Long id) {
        if (!noticeRepository.existsById(id)) throw new ResourceNotFoundException("Notice not found");
        noticeRepository.deleteById(id);
    }

    public Notice getNotice(Long id) {
        return noticeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Notice not found"));
    }

    public List<Notice> getAllNotices() {
        return noticeRepository.findAllByOrderByCreatedAtDesc();
    }
}
