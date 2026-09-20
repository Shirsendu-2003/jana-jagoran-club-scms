package com.janajagoran.scms.repository;

import com.janajagoran.scms.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByUserIdAndPurposeOrderByRequestedAtDesc(Long userId, String purpose);
    List<OtpVerification> findByUserIdOrderByRequestedAtDesc(Long userId);
}
