package com.janajagoran.scms.repository;

import com.janajagoran.scms.entity.DesignationPostDetail;
import com.janajagoran.scms.enums.DesignationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DesignationPostDetailRepository extends JpaRepository<DesignationPostDetail, Long> {
    Optional<DesignationPostDetail> findByDesignationId(Long designationId);
    List<DesignationPostDetail> findByStatus(DesignationStatus status);
}
