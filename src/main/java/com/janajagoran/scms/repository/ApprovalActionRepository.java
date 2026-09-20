package com.janajagoran.scms.repository;

import com.janajagoran.scms.entity.ApprovalAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalActionRepository extends JpaRepository<ApprovalAction, Long> {
    List<ApprovalAction> findByRequestId(Long requestId);
}
