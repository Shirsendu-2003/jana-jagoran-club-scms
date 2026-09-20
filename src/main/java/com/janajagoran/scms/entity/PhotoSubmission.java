package com.janajagoran.scms.entity;

import com.janajagoran.scms.enums.PhotoSubmissionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A photo submitted from the public landing page. Deliberately NOT tied to GalleryImage:
 * submitters may be anonymous non-users, so we capture a free-text photographer name rather
 * than a User FK, and we keep the moderation trail (who approved/rejected, when, why) here.
 *
 * `submittedByUser` is populated only when a logged-in member happens to submit -- for
 * everyone else it stays null and `photographerName` is the only attribution we have.
 *
 * Nothing here is publicly visible until status = APPROVED.
 */
@Entity
@Table(name = "photo_submissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PhotoSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    /** Free-text: anonymous submitters have no account to attribute this to. */
    @Column(name = "photographer_name", nullable = false, length = 150)
    private String photographerName;

    @Column(length = 500)
    private String caption;

    @Column(length = 200)
    private String location;

    /** Optional -- lets moderators follow up on a submission if they need to. */
    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    /** Null for anonymous/public submissions; set when a logged-in user submits. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "submitted_by_user")
    private User submittedByUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PhotoSubmissionStatus status = PhotoSubmissionStatus.PENDING;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    /** Kept for abuse tracing on an unauthenticated endpoint. */
    @Column(name = "submitter_ip", length = 50)
    private String submitterIp;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }
}
