package com.janajagoran.scms.repository;

import com.janajagoran.scms.entity.Budget;
import com.janajagoran.scms.enums.BudgetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByStatus(BudgetStatus status);
    List<Budget> findByFiscalYear(Integer fiscalYear);
}
