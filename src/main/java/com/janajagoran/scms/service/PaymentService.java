package com.janajagoran.scms.service;

import com.janajagoran.scms.dto.MemberPaymentSummaryDto;
import com.janajagoran.scms.dto.PaymentRecordRequest;
import com.janajagoran.scms.entity.Member;
import com.janajagoran.scms.entity.Payment;
import com.janajagoran.scms.entity.PaymentReceipt;
import com.janajagoran.scms.entity.User;
import com.janajagoran.scms.enums.PaymentMode;
import com.janajagoran.scms.enums.PaymentStatus;
import com.janajagoran.scms.exception.ResourceNotFoundException;
import com.janajagoran.scms.repository.MemberRepository;
import com.janajagoran.scms.repository.PaymentReceiptRepository;
import com.janajagoran.scms.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final PaymentReceiptRepository paymentReceiptRepository;

    private static final String[] MONTH_NAMES = new String[]{
            "January","February","March","April","May","June",
            "July","August","September","October","November","December"};

    /** Ensures a payment row exists (as UNPAID) for the member for the given month/year, returns it. */
    @Transactional
    public Payment getOrCreatePaymentRow(Member member, int month, int year) {
        return paymentRepository.findByMemberIdAndPaymentMonthAndPaymentYear(member.getId(), month, year)
                .orElseGet(() -> paymentRepository.save(Payment.builder()
                        .member(member)
                        .paymentMonth(month)
                        .paymentYear(year)
                        .amount(member.getMonthlyFee())
                        .status(PaymentStatus.UNPAID)
                        .build()));
    }

    public List<Payment> getPaymentHistory(Long memberId) {
        return paymentRepository.findByMemberId(memberId);
    }

    /** Computes pending months for a member from their joining date/year up to the current month. */
    public MemberPaymentSummaryDto getPendingSummary(Member member) {
        LocalDate now = LocalDate.now();
        LocalDate start = member.getDateOfJoining() != null ? member.getDateOfJoining() : now.withMonth(1).withDayOfMonth(1);

        List<String> pendingMonths = new ArrayList<>();
        BigDecimal totalDue = BigDecimal.ZERO;

        LocalDate cursor = start.withDayOfMonth(1);
        while (!cursor.isAfter(now.withDayOfMonth(1))) {
            int m = cursor.getMonthValue();
            int y = cursor.getYear();
            boolean paid = paymentRepository.findByMemberIdAndPaymentMonthAndPaymentYear(member.getId(), m, y)
                    .map(p -> p.getStatus() == PaymentStatus.PAID)
                    .orElse(false);
            if (!paid) {
                pendingMonths.add(MONTH_NAMES[m - 1] + " " + y);
                totalDue = totalDue.add(member.getMonthlyFee());
            }
            cursor = cursor.plusMonths(1);
        }

        return MemberPaymentSummaryDto.builder()
                .memberId(member.getId())
                .membershipId(member.getMembershipId())
                .memberName(member.getUser().getName())
                .totalDue(totalDue)
                .pendingMonthsCount(pendingMonths.size())
                .pendingMonths(pendingMonths)
                .build();
    }

    @Transactional
    public Payment recordPayment(PaymentRecordRequest request, User recordedBy) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        Payment payment = getOrCreatePaymentRow(member, request.getMonth(), request.getYear());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidOn(java.time.LocalDateTime.now());
        payment.setAmount(request.getAmount() != null ? request.getAmount() : member.getMonthlyFee());
        payment.setPaymentMode(request.getPaymentMode() != null ? PaymentMode.valueOf(request.getPaymentMode()) : PaymentMode.CASH);
        payment.setRecordedBy(recordedBy);
        payment = paymentRepository.save(payment);

        String receiptNumber = "RCPT-" + payment.getPaymentYear() + "-" + String.format("%06d", payment.getId());
        PaymentReceipt receipt = PaymentReceipt.builder()
                .payment(payment)
                .receiptNumber(receiptNumber)
                .build();
        paymentReceiptRepository.save(receipt);

        return payment;
    }

    public long countPending() {
        return paymentRepository.countByStatus(PaymentStatus.UNPAID);
    }

    public BigDecimal totalCollectedThisMonth() {
        LocalDate now = LocalDate.now();
        return paymentRepository.totalCollectedForMonth(now.getMonthValue(), now.getYear());
    }
}
