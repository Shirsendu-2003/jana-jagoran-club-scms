package com.janajagoran.scms.service;

import com.janajagoran.scms.dto.DashboardStatsDto;
import com.janajagoran.scms.enums.MemberStatus;
import com.janajagoran.scms.repository.EventRepository;
import com.janajagoran.scms.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MemberRepository memberRepository;
    private final EventRepository eventRepository;
    private final PaymentService paymentService;
    private final BudgetService budgetService;

    public DashboardStatsDto getStats() {
        int year = LocalDate.now().getYear();
        BigDecimal income = budgetService.totalIncomeForYear(year);
        BigDecimal expense = budgetService.totalExpenseForYear(year);

        return DashboardStatsDto.builder()
                .totalMembers(memberRepository.count())
                .activeMembers(memberRepository.countByStatus(MemberStatus.ACTIVE))
                .pendingPayments(paymentService.countPending())
                .pendingRegistrations(memberRepository.countByStatus(MemberStatus.PENDING))
                .totalEvents(eventRepository.count())
                .totalIncome(income)
                .totalExpense(expense)
                .netBalance(income.subtract(expense))
                .monthlyCollected(paymentService.totalCollectedThisMonth())
                .build();
    }
}
