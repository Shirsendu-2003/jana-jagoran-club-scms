package com.janajagoran.scms.repository;

import com.janajagoran.scms.entity.DesignationMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DesignationMasterRepository extends JpaRepository<DesignationMaster, Long> {
    Optional<DesignationMaster> findByTitleIgnoreCase(String title);
    boolean existsByTitleIgnoreCase(String title);
}
