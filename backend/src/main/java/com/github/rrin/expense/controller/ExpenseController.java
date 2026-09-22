package com.github.rrin.expense.controller;

import com.github.rrin.expense.Expense;
import com.github.rrin.expense.dto.ExpenseRequest;
import com.github.rrin.expense.dto.ExpenseResponse;
import com.github.rrin.expense.dto.filter.ExpenseFilter;
import com.github.rrin.expense.service.ExpenseService;
import com.github.rrin.utils.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/expense")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/{id}")
    public ExpenseResponse get(@PathVariable UUID id) {
        return toResponse(expenseService.get(id));
    }

    @PostMapping
    public ExpenseResponse create(@RequestBody ExpenseRequest request) {
        return toResponse(expenseService.create(request));
    }

    @PutMapping
    public ExpenseResponse update(@RequestBody ExpenseRequest request) {
        return toResponse(expenseService.update(request));
    }

    @DeleteMapping("/{id}")
    public ExpenseResponse delete(@PathVariable UUID id) {
        return toResponse(expenseService.delete(id));
    }

    @GetMapping
    public PaginatedResponse<ExpenseResponse> search(@ModelAttribute ExpenseFilter filter,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return PaginatedResponse.of(expenseService.search(filter, page, size).map(this::toResponse));
    }

    @GetMapping("/sum")
    public BigDecimal sum(@ModelAttribute ExpenseFilter filter) {
        return expenseService.sum(filter);
    }

    private ExpenseResponse toResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .employeeId(expense.getEmployee() != null ? expense.getEmployee().getId() : null)
                .amount(expense.getAmount())
                .date(expense.getDate())
                .description(expense.getDescription())
                .build();
    }
}
