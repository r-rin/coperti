package com.github.rrin.expense.controller;

import com.github.rrin.expense.Expense;
import com.github.rrin.expense.Reimbursement;
import com.github.rrin.expense.dto.ReimbursementRequest;
import com.github.rrin.expense.dto.ReimbursementResponse;
import com.github.rrin.expense.dto.filter.ReimbursementFilter;
import com.github.rrin.expense.service.ReimbursementService;
import com.github.rrin.utils.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/reimbursement")
public class ReimbursementController {

    private final ReimbursementService reimbursementService;

    @Autowired
    public ReimbursementController(ReimbursementService reimbursementService) {
        this.reimbursementService = reimbursementService;
    }

    @GetMapping("/{id}")
    public ReimbursementResponse get(@PathVariable UUID id) {
        return toResponse(reimbursementService.get(id));
    }

    @PostMapping
    public ReimbursementResponse create(@RequestBody ReimbursementRequest request) {
        return toResponse(reimbursementService.create(request));
    }

    // raises whatever funding left uncovered on the expense, rather than trusting a caller-supplied amount
    @PostMapping("/shortfall/{expenseId}")
    public ReimbursementResponse createForShortfall(@PathVariable UUID expenseId) {
        return toResponse(reimbursementService.createForShortfall(expenseId));
    }

    @PutMapping
    public ReimbursementResponse update(@RequestBody ReimbursementRequest request) {
        return toResponse(reimbursementService.update(request));
    }

    // pays the debt through a new disbursement and its funding row; id is taken from the body
    @PutMapping("/paid")
    public ReimbursementResponse markAsPaid(@RequestBody ReimbursementRequest request) {
        return toResponse(reimbursementService.markAsPaid(request.getId()));
    }

    @DeleteMapping("/{id}")
    public ReimbursementResponse delete(@PathVariable UUID id) {
        return toResponse(reimbursementService.delete(id));
    }

    @GetMapping
    public PaginatedResponse<ReimbursementResponse> search(@ModelAttribute ReimbursementFilter filter,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        return PaginatedResponse.of(reimbursementService.search(filter, page, size).map(this::toResponse));
    }

    @GetMapping("/sum")
    public BigDecimal sum(@ModelAttribute ReimbursementFilter filter) {
        return reimbursementService.sum(filter);
    }

    private ReimbursementResponse toResponse(Reimbursement reimbursement) {
        Expense expense = reimbursement.getExpense();
        return ReimbursementResponse.builder()
                .id(reimbursement.getId())
                .expenseId(expense != null ? expense.getId() : null)
                .employeeId(expense != null && expense.getEmployee() != null ? expense.getEmployee().getId() : null)
                .amount(reimbursement.getAmount())
                .status(reimbursement.getStatus())
                // reading only the id keeps the lazy payout proxy uninitialised
                .payoutDisbursementId(reimbursement.getPayoutDisbursement() != null
                        ? reimbursement.getPayoutDisbursement().getId()
                        : null)
                .build();
    }
}
