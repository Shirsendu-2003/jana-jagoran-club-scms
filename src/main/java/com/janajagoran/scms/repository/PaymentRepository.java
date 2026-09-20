package com.janajagoran.scms.repository;

import com.janajagoran.scms.entity.Payment;
import com.janajagoran.scms.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByMemberId(Long memberId);
    Optional<Payment> findByMemberIdAndPaymentMonthAndPaymentYear(Long memberId, Integer month, Integer year);
    List<Payment> findByPaymentMonthAndPaymentYear(Integer month, Integer year);
    long countByStatus(PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID' AND p.paymentMonth = :month AND p.paymentYear = :year")
    java.math.BigDecimal totalCollectedForMonth(@Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID' AND p.paymentYear = :year")
    java.math.BigDecimal totalCollectedForYear(@Param("year") Integer year);
}
