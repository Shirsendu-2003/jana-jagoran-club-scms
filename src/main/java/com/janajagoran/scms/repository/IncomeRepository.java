package com.janajagoran.scms.repository;

import com.janajagoran.scms.entity.Income;
import com.janajagoran.scms.enums.IncomeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findByBudgetId(Long budgetId);
    List<Income> findByCategory(IncomeCategory category);
    List<Income> findByIncomeDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(i.amount),0) FROM Income i WHERE i.incomeDate BETWEEN :start AND :end")
    BigDecimal sumBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT i.category, COALESCE(SUM(i.amount),0) FROM Income i WHERE i.incomeDate BETWEEN :start AND :end GROUP BY i.category")
    List<Object[]> sumByCategoryBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
