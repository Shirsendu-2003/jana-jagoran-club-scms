package com.janajagoran.scms.entity;

import com.janajagoran.scms.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "members")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "membership_id", nullable = false, unique = true, length = 50)
    private String membershipId;

    @Column(length = 500)
    private String address;

    @Column(name = "date_of_joining")
    private LocalDate dateOfJoining;

    @Column(name = "monthly_fee", nullable = false)
    @Builder.Default
    private BigDecimal monthlyFee = new BigDecimal("50.00");

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;

    /** Reason captured from the "modal reason -> save" step whenever an Admin flips this member Inactive. */
    @Column(name = "deactivation_reason", length = 500)
    private String deactivationReason;

    @PrePersist
    protected void onCreate() {
        if (dateOfJoining == null) dateOfJoining = LocalDate.now();
    }
}
