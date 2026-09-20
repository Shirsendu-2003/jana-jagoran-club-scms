package com.janajagoran.scms.repository;

import com.janajagoran.scms.entity.MemberDesignationDetail;
import com.janajagoran.scms.enums.MemberDesignationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberDesignationDetailRepository extends JpaRepository<MemberDesignationDetail, Long> {
    List<MemberDesignationDetail> findByMemberId(Long memberId);
    List<MemberDesignationDetail> findByDesignationId(Long designationId);
    Optional<MemberDesignationDetail> findByDesignationIdAndStatus(Long designationId, MemberDesignationStatus status);
    List<MemberDesignationDetail> findByStatus(MemberDesignationStatus status);
    List<MemberDesignationDetail> findByMemberIdAndStatus(Long memberId, MemberDesignationStatus status);
}
