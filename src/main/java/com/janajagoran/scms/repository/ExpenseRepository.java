package com.janajagoran.scms.repository;

import com.janajagoran.scms.entity.Expense;
import com.janajagoran.scms.enums.ApprovalStatus;
import com.janajagoran.scms.enums.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByBudgetId(Long budgetId);
    List<Expense> findByStatus(ApprovalStatus status);
    List<Expense> findByCategory(ExpenseCategory category);
    List<Expense> findByExpenseDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount),0) FROM Expense e WHERE e.status = 'APPROVED' AND e.expenseDate BETWEEN :start AND :end")
    BigDecimal sumApprovedBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT e.category, COALESCE(SUM(e.amount),0) FROM Expense e WHERE e.status = 'APPROVED' AND e.expenseDate BETWEEN :start AND :end GROUP BY e.category")
    List<Object[]> sumByCategoryBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
