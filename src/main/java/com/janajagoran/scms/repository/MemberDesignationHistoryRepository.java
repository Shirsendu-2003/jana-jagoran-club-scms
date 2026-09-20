package com.janajagoran.scms.repository;

import com.janajagoran.scms.entity.MemberDesignationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface MemberDesignationHistoryRepository extends JpaRepository<MemberDesignationHistory, Long>, JpaSpecificationExecutor<MemberDesignationHistory> {
    List<MemberDesignationHistory> findByMemberIdOrderByAssignmentDateDesc(Long memberId);

    /** The currently-open episode (not yet released) for a member+designation, if any. */
    Optional<MemberDesignationHistory> findByMemberIdAndDesignationIdAndReleaseDateIsNull(Long memberId, Long designationId);

    Page<MemberDesignationHistory> findAllByOrderByAssignmentDateDesc(Pageable pageable);

    List<MemberDesignationHistory> findByReleaseDateIsNull();
}
