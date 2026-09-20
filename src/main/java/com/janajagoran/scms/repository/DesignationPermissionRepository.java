package com.janajagoran.scms.repository;

import com.janajagoran.scms.entity.DesignationPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DesignationPermissionRepository extends JpaRepository<DesignationPermission, Long> {
    List<DesignationPermission> findByPostDetailId(Long postDetailId);
    void deleteByPostDetailId(Long postDetailId);
}
