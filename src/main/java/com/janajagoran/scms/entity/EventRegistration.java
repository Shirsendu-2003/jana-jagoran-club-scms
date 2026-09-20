package com.janajagoran.scms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_registrations", uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "member_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Builder.Default
    private Boolean attended = false;

    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
    }
}
