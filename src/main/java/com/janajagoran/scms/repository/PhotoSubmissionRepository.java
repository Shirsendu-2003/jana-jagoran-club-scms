package com.janajagoran.scms.repository;

import com.janajagoran.scms.entity.PhotoSubmission;
import com.janajagoran.scms.enums.PhotoSubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PhotoSubmissionRepository extends JpaRepository<PhotoSubmission, Long> {
    List<PhotoSubmission> findByStatusOrderBySubmittedAtDesc(PhotoSubmissionStatus status);

    /** Rate limiting: how many submissions has this IP made recently? */
    @Query("SELECT COUNT(p) FROM PhotoSubmission p WHERE p.submitterIp = :ip AND p.submittedAt > :since")
    long countRecentByIp(@Param("ip") String ip, @Param("since") LocalDateTime since);

    long countByStatus(PhotoSubmissionStatus status);
}
