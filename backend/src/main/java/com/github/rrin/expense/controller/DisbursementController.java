package com.github.rrin.expense.controller;

import com.github.rrin.expense.Disbursement;
import com.github.rrin.expense.dto.DisbursementRequest;
import com.github.rrin.expense.dto.DisbursementResponse;
import com.github.rrin.expense.dto.filter.DisbursementFilter;
import com.github.rrin.expense.service.DisbursementService;
import com.github.rrin.utils.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/disbursement")
public class DisbursementController {

    private final DisbursementService disbursementService;

    @Autowired
    public DisbursementController(DisbursementService disbursementService) {
        this.disbursementService = disbursementService;
    }

    @GetMapping("/{id}")
    public DisbursementResponse get(@PathVariable UUID id) {
        return toResponse(disbursementService.getById(id));
    }

    @PostMapping
    public DisbursementResponse create(@RequestBody DisbursementRequest request) {
        return toResponse(disbursementService.create(request));
    }

    // only the status is mutable; id and status are taken from the body
    @PutMapping("/status")
    public DisbursementResponse updateStatus(@RequestBody DisbursementRequest request) {
        return toResponse(disbursementService.updateStatus(request.getId(), request.getStatus()));
    }

    @GetMapping
    public PaginatedResponse<DisbursementResponse> search(@ModelAttribute DisbursementFilter filter,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        return PaginatedResponse.of(disbursementService.search(filter, page, size).map(this::toResponse));
    }

    @GetMapping("/sum")
    public BigDecimal sum(@ModelAttribute DisbursementFilter filter) {
        return disbursementService.sum(filter);
    }

    private DisbursementResponse toResponse(Disbursement disbursement) {
        return DisbursementResponse.builder()
                .id(disbursement.getId())
                .employeeId(disbursement.getEmployee().getId())
                .amount(disbursement.getAmount())
                .date(disbursement.getDate())
                .status(disbursement.getStatus())
                .build();
    }
}
