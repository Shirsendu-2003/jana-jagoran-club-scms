package com.janajagoran.scms.service;

import com.janajagoran.scms.dto.PhotoSubmissionResponse;
import com.janajagoran.scms.entity.PhotoSubmission;
import com.janajagoran.scms.entity.User;
import com.janajagoran.scms.enums.AuditAction;
import com.janajagoran.scms.enums.PhotoSubmissionStatus;
import com.janajagoran.scms.exception.BadRequestException;
import com.janajagoran.scms.exception.ResourceNotFoundException;
import com.janajagoran.scms.repository.PhotoSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Public photo submissions. This is the only unauthenticated write path in the whole
 * application, so it's deliberately defensive:
 *   - allow-list of image MIME types + extensions (no SVG -- it can carry scripts)
 *   - hard size cap independent of the global multipart limit
 *   - per-IP rate limit
 *   - submitter IP recorded for abuse tracing
 * Nothing is publicly visible until a moderator approves it.
 */
@Service
@RequiredArgsConstructor
public class PhotoSubmissionService {

    private final PhotoSubmissionRepository repository;
    private final FileStorageService fileStorageService;
    private final AuditLogService auditLogService;

    /** SVG is intentionally excluded -- it's an XML document that can embed scripts. */
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/heic");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp", ".heic");
    private static final long MAX_BYTES = 8L * 1024 * 1024; // 8 MB
    private static final int MAX_SUBMISSIONS_PER_IP_PER_HOUR = 5;

    @Transactional
    public PhotoSubmission submit(MultipartFile file, String photographerName, String caption,
                                   String location, String contactEmail, User submittedByUser, String ip) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please choose a photo to upload");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BadRequestException("Photo is too large. Please upload an image under 8 MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Only JPG, PNG, WEBP or HEIC images are accepted");
        }

        String original = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        boolean extOk = ALLOWED_EXTENSIONS.stream().anyMatch(original::endsWith);
        if (!extOk) {
            throw new BadRequestException("Only JPG, PNG, WEBP or HEIC images are accepted");
        }

        if (photographerName == null || photographerName.isBlank()) {
            throw new BadRequestException("Please tell us who took the photo");
        }

        // Rate limit unauthenticated abuse. Logged-in members are exempt -- they're already accountable.
        if (submittedByUser == null && ip != null) {
            long recent = repository.countRecentByIp(ip, LocalDateTime.now().minusHours(1));
            if (recent >= MAX_SUBMISSIONS_PER_IP_PER_HOUR) {
                throw new BadRequestException("You've submitted several photos recently. Please try again later.");
            }
        }

        String url = fileStorageService.store(file, "submissions");

        PhotoSubmission submission = PhotoSubmission.builder()
                .imageUrl(url)
                .photographerName(photographerName.trim())
                .caption(caption != null ? caption.trim() : null)
                .location(location != null ? location.trim() : null)
                .contactEmail(contactEmail != null ? contactEmail.trim() : null)
                .submittedByUser(submittedByUser)
                .status(PhotoSubmissionStatus.PENDING)
                .submitterIp(ip)
                .build();

        PhotoSubmission saved = repository.save(submission);

        auditLogService.log(AuditAction.PHOTO_SUBMITTED, submittedByUser, "PhotoSubmission", saved.getId(),
                "Photo submitted by " + saved.getPhotographerName()
                        + (submittedByUser == null ? " (anonymous public submission)" : ""),
                null, PhotoSubmissionStatus.PENDING.name(), null, saved.getPhotographerName());

        return saved;
    }

    @Transactional
    public PhotoSubmission review(Long id, boolean approve, String rejectionReason, User reviewer) {
        PhotoSubmission submission = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Photo submission not found"));

        if (submission.getStatus() != PhotoSubmissionStatus.PENDING) {
            throw new BadRequestException("This submission has already been reviewed");
        }

        String previous = submission.getStatus().name();
        submission.setStatus(approve ? PhotoSubmissionStatus.APPROVED : PhotoSubmissionStatus.REJECTED);
        submission.setReviewedBy(reviewer);
        submission.setReviewedAt(LocalDateTime.now());
        if (!approve) submission.setRejectionReason(rejectionReason);
        repository.save(submission);

        auditLogService.log(
                approve ? AuditAction.PHOTO_APPROVED : AuditAction.PHOTO_REJECTED,
                reviewer, "PhotoSubmission", submission.getId(),
                approve ? "Public photo submission approved" : ("Public photo submission rejected: " + rejectionReason),
                previous, submission.getStatus().name(), null, submission.getPhotographerName());

        return submission;
    }

    @Transactional
    public PhotoSubmission setFeatured(Long id, boolean featured) {
        PhotoSubmission submission = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Photo submission not found"));
        if (submission.getStatus() != PhotoSubmissionStatus.APPROVED) {
            throw new BadRequestException("Only an approved photo can be featured");
        }
        submission.setIsFeatured(featured);
        return repository.save(submission);
    }

    @Transactional
    public void delete(Long id) {
        PhotoSubmission submission = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Photo submission not found"));
        fileStorageService.delete(submission.getImageUrl());
        repository.delete(submission);
    }

    public List<PhotoSubmission> getPending() {
        return repository.findByStatusOrderBySubmittedAtDesc(PhotoSubmissionStatus.PENDING);
    }

    public List<PhotoSubmission> getApproved() {
        return repository.findByStatusOrderBySubmittedAtDesc(PhotoSubmissionStatus.APPROVED);
    }

    public long countPending() {
        return repository.countByStatus(PhotoSubmissionStatus.PENDING);
    }

    /** Public landing-page feed -- approved only, stripped of contact/IP/reviewer data. */
    public List<PhotoSubmissionResponse> getPublicFeed(int limit) {
        return repository.findByStatusOrderBySubmittedAtDesc(PhotoSubmissionStatus.APPROVED).stream()
                .limit(limit)
                .map(this::toPublicDto)
                .toList();
    }

    private PhotoSubmissionResponse toPublicDto(PhotoSubmission p) {
        return PhotoSubmissionResponse.builder()
                .id(p.getId())
                .imageUrl(p.getImageUrl())
                .photographerName(p.getPhotographerName())
                .caption(p.getCaption())
                .location(p.getLocation())
                .submittedAt(p.getSubmittedAt())
                .isFeatured(p.getIsFeatured())
                .build();
    }
}
