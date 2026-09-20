package com.janajagoran.scms.repository;

import com.janajagoran.scms.entity.Member;
import com.janajagoran.scms.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUserId(Long userId);
    Optional<Member> findByMembershipId(String membershipId);
    long countByStatus(MemberStatus status);

    @Query("SELECT m FROM Member m WHERE " +
           "LOWER(m.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "m.user.email LIKE CONCAT('%', :keyword, '%') OR " +
           "m.user.phone LIKE CONCAT('%', :keyword, '%') OR " +
           "m.membershipId LIKE CONCAT('%', :keyword, '%')")
    List<Member> search(@Param("keyword") String keyword);

    List<Member> findByStatus(MemberStatus status);
}
