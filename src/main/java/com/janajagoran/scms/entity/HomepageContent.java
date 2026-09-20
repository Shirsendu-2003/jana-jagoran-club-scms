package com.janajagoran.scms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Editable content blocks for the public landing page (e.g. HERO, ABOUT, ANNOUNCEMENT), editable by Admin or a designated person with HOMEPAGE_EDIT permission. */
@Entity
@Table(name = "homepage_content")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HomepageContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "section_key", nullable = false, unique = true, length = 50)
    private String sectionKey; // e.g. HERO, ABOUT, ANNOUNCEMENT

    @Column(length = 200)
    private String title;

    @Column(length = 4000)
    private String body;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    @PrePersist
    protected void onSave() {
        updatedAt = LocalDateTime.now();
    }
}
