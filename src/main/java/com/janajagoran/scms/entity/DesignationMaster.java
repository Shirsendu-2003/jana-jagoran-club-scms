package com.janajagoran.scms.entity;

import com.janajagoran.scms.enums.DesignationLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** desg_master -- the static catalog entry for a designation/post (e.g. "President", "Cultural Secretary"). */
@Entity
@Table(name = "desg_master")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DesignationMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DesignationLevel level = DesignationLevel.LOWER_LEVEL;

    /** The "operator" who proposed this designation -- any staff role can propose; an Admin approves it via DesignationPostDetail. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
