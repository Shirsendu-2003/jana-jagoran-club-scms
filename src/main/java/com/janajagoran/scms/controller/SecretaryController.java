package com.janajagoran.scms.controller;

import com.janajagoran.scms.dto.*;
import com.janajagoran.scms.entity.*;
import com.janajagoran.scms.security.UserPrincipal;
import com.janajagoran.scms.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/** Endpoints for the Secretary role: budgets, income/expenses, events, gallery, notices, payment reports. */
@RestController
@RequestMapping("/api/secretary")
@RequiredArgsConstructor
public class SecretaryController {

    private final BudgetService budgetService;
    private final EventService eventService;
    private final GalleryService galleryService;
    private final PaymentService paymentService;
    private final DashboardService dashboardService;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> dashboard() {
        return ResponseEntity.ok(ApiResponse.ok("Dashboard fetched", dashboardService.getStats()));
    }

    // ---------------- Budget ----------------

    @PostMapping("/budgets")
    public ResponseEntity<ApiResponse<Budget>> createBudget(
            @Valid @RequestBody BudgetRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getReference(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Budget created and sent for approval", budgetService.createBudget(request, user)));
    }

    @PutMapping("/budgets/{id}")
    public ResponseEntity<ApiResponse<Budget>> updateBudget(@PathVariable Long id, @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Budget updated", budgetService.updateBudget(id, request)));
    }

    @DeleteMapping("/budgets/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.ok(ApiResponse.ok("Budget deleted"));
    }

    @GetMapping("/budgets")
    public ResponseEntity<ApiResponse<List<Budget>>> budgets() {
        return ResponseEntity.ok(ApiResponse.ok("Budgets fetched", budgetService.getAllBudgets()));
    }

    // ---------------- Income ----------------

    @PostMapping("/income")
    public ResponseEntity<ApiResponse<Income>> addIncome(
            @Valid @RequestBody IncomeRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getReference(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Income recorded", budgetService.addIncome(request, user)));
    }

    @GetMapping("/income")
    public ResponseEntity<ApiResponse<List<Income>>> income() {
        return ResponseEntity.ok(ApiResponse.ok("Income fetched", budgetService.getAllIncome()));
    }

    // ---------------- Expenses ----------------

    @PostMapping("/expenses")
    public ResponseEntity<ApiResponse<Expense>> addExpense(
            @Valid @RequestBody ExpenseRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getReference(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Expense submitted for approval", budgetService.addExpense(request, user)));
    }

    @PatchMapping("/expenses/{id}/approve")
    public ResponseEntity<ApiResponse<Expense>> approveExpense(
            @PathVariable Long id, @RequestParam boolean approve, @AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getReference(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Expense status updated", budgetService.approveExpense(id, user, approve)));
    }

    @GetMapping("/expenses")
    public ResponseEntity<ApiResponse<List<Expense>>> expenses() {
        return ResponseEntity.ok(ApiResponse.ok("Expenses fetched", budgetService.getAllExpenses()));
    }

    // ---------------- Events ----------------

    @PostMapping("/events")
    public ResponseEntity<ApiResponse<Event>> createEvent(
            @Valid @RequestBody EventRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getReference(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Event created", eventService.createEvent(request, user)));
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<ApiResponse<Event>> updateEvent(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Event updated", eventService.updateEvent(id, request)));
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.ok("Event deleted"));
    }

    @GetMapping("/events/{id}/registrations")
    public ResponseEntity<ApiResponse<List<EventRegistration>>> registrations(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Registrations fetched", eventService.getRegistrations(id)));
    }

    // ---------------- Gallery management ----------------

    @PostMapping("/gallery/albums")
    public ResponseEntity<ApiResponse<Gallery>> createAlbum(
            @RequestParam String title, @RequestParam(required = false) Long eventId,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getReference(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Album created", galleryService.createAlbum(title, eventId, user)));
    }

    // ---------------- Payment reports ----------------

    @PostMapping("/payments/record")
    public ResponseEntity<ApiResponse<Payment>> recordPayment(
            @Valid @RequestBody PaymentRecordRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getReference(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Payment recorded successfully", paymentService.recordPayment(request, user)));
    }

    @GetMapping("/payments/pending-summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> pendingSummary() {
        Map<String, Object> data = Map.of(
                "pendingCount", paymentService.countPending(),
                "collectedThisMonth", paymentService.totalCollectedThisMonth()
        );
        return ResponseEntity.ok(ApiResponse.ok("Payment summary fetched", data));
    }
}
