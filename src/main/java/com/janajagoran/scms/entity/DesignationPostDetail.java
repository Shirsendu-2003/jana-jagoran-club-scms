package com.janajagoran.scms.entity;

import com.janajagoran.scms.enums.DesignationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** desg_post_details -- the lifecycle/approval state of a designation: creation -> active -> inactive. */
@Entity
@Table(name = "desg_post_details")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DesignationPostDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "designation_id", nullable = false, unique = true)
    private DesignationMaster designation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DesignationStatus status = DesignationStatus.CREATION;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
