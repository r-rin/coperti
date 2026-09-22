package com.github.rrin.expense.controller;

import com.github.rrin.expense.Budget;
import com.github.rrin.expense.dto.BudgetRequest;
import com.github.rrin.expense.dto.BudgetResponse;
import com.github.rrin.expense.dto.filter.BudgetFilter;
import com.github.rrin.expense.service.BudgetService;
import com.github.rrin.utils.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {

    private final BudgetService budgetService;

    @Autowired
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping("/{id}")
    public BudgetResponse get(@PathVariable UUID id) {
        return toResponse(budgetService.getById(id));
    }

    @PostMapping
    public BudgetResponse create(@RequestBody BudgetRequest request) {
        return toResponse(budgetService.create(request));
    }

    @GetMapping
    public PaginatedResponse<BudgetResponse> search(@ModelAttribute BudgetFilter filter,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return PaginatedResponse.of(budgetService.search(filter, page, size).map(this::toResponse));
    }

    @GetMapping("/sum")
    public BigDecimal sum(@ModelAttribute BudgetFilter filter) {
        return budgetService.sum(filter);
    }

    private BudgetResponse toResponse(Budget budget) {
        return BudgetResponse.builder()
                .id(budget.getId())
                .amount(budget.getAmount())
                .givenBy(budget.getGivenBy())
                .description(budget.getDescription())
                .date(budget.getDate())
                .build();
    }
}
