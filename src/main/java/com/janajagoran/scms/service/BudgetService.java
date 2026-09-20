package com.janajagoran.scms.service;

import com.janajagoran.scms.dto.BudgetRequest;
import com.janajagoran.scms.dto.ExpenseRequest;
import com.janajagoran.scms.dto.IncomeRequest;
import com.janajagoran.scms.entity.*;
import com.janajagoran.scms.enums.ApprovalStatus;
import com.janajagoran.scms.enums.BudgetStatus;
import com.janajagoran.scms.enums.ExpenseCategory;
import com.janajagoran.scms.enums.IncomeCategory;
import com.janajagoran.scms.exception.BadRequestException;
import com.janajagoran.scms.exception.ResourceNotFoundException;
import com.janajagoran.scms.repository.BudgetRepository;
import com.janajagoran.scms.repository.ExpenseRepository;
import com.janajagoran.scms.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    // ---------------- Budgets ----------------

    @Transactional
    public Budget createBudget(BudgetRequest request, User createdBy) {
        Budget budget = Budget.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .fiscalYear(request.getFiscalYear())
                .totalAmount(request.getTotalAmount() != null ? request.getTotalAmount() : BigDecimal.ZERO)
                .status(BudgetStatus.PENDING_APPROVAL)
                .createdBy(createdBy)
                .build();
        return budgetRepository.save(budget);
    }

    @Transactional
    public Budget updateBudget(Long id, BudgetRequest request) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        budget.setTitle(request.getTitle());
        budget.setDescription(request.getDescription());
        budget.setFiscalYear(request.getFiscalYear());
        if (request.getTotalAmount() != null) budget.setTotalAmount(request.getTotalAmount());
        return budgetRepository.save(budget);
    }

    @Transactional
    public void deleteBudget(Long id) {
        if (!budgetRepository.existsById(id)) throw new ResourceNotFoundException("Budget not found");
        budgetRepository.deleteById(id);
    }

    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    public Budget getBudget(Long id) {
        return budgetRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
    }

    @Transactional
    public Budget approveBudget(Long id, User approver, boolean approve) {
        Budget budget = getBudget(id);
        budget.setStatus(approve ? BudgetStatus.APPROVED : BudgetStatus.REJECTED);
        budget.setApprovedBy(approver);
        return budgetRepository.save(budget);
    }

    // ---------------- Income ----------------

    @Transactional
    public Income addIncome(IncomeRequest request, User recordedBy) {
        Budget budget = null;
        if (request.getBudgetId() != null) budget = getBudget(request.getBudgetId());

        IncomeCategory category;
        try {
            category = IncomeCategory.valueOf(request.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid income category");
        }

        Income income = Income.builder()
                .budget(budget)
                .category(category)
                .amount(request.getAmount())
                .description(request.getDescription())
                .incomeDate(request.getIncomeDate())
                .recordedBy(recordedBy)
                .build();
        return incomeRepository.save(income);
    }

    public List<Income> getAllIncome() {
        return incomeRepository.findAll();
    }

    // ---------------- Expenses ----------------

    @Transactional
    public Expense addExpense(ExpenseRequest request, User recordedBy) {
        Budget budget = null;
        if (request.getBudgetId() != null) budget = getBudget(request.getBudgetId());

        ExpenseCategory category;
        try {
            category = ExpenseCategory.valueOf(request.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid expense category");
        }

        Expense expense = Expense.builder()
                .budget(budget)
                .category(category)
                .amount(request.getAmount())
                .description(request.getDescription())
                .expenseDate(request.getExpenseDate())
                .status(ApprovalStatus.PENDING)
                .recordedBy(recordedBy)
                .build();
        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense approveExpense(Long id, User approver, boolean approve) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        expense.setStatus(approve ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED);
        expense.setApprovedBy(approver);
        return expenseRepository.save(expense);
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public List<Expense> getPendingExpenses() {
        return expenseRepository.findByStatus(ApprovalStatus.PENDING);
    }

    // ---------------- Reporting helpers ----------------

    public BigDecimal totalIncomeForYear(int year) {
        return incomeRepository.sumBetween(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
    }

    public BigDecimal totalExpenseForYear(int year) {
        return expenseRepository.sumApprovedBetween(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
    }
}
