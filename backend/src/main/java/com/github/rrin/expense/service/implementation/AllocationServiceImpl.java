package com.github.rrin.expense.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.expense.Disbursement;
import com.github.rrin.expense.Funding;
import com.github.rrin.expense.dto.AllocationRequest;
import com.github.rrin.expense.dto.AllocationSelection;
import com.github.rrin.expense.dto.FundingRequest;
import com.github.rrin.expense.service.AllocationResult;
import com.github.rrin.expense.service.AllocationService;
import com.github.rrin.expense.service.DisbursementService;
import com.github.rrin.expense.service.FundingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AllocationServiceImpl implements AllocationService {

    private final FundingService fundingService;
    private final DisbursementService disbursementService;

    @Autowired
    public AllocationServiceImpl(FundingService fundingService, DisbursementService disbursementService) {
        this.fundingService = fundingService;
        this.disbursementService = disbursementService;
    }

    @Override
    @Transactional
    public AllocationResult allocateManual(AllocationRequest request) {
        validateShape(request);

        Disbursement disbursement = disbursementService.getById(request.getDisbursementId());
        BigDecimal remaining = fundingService.getRemainingBalance(disbursement.getId());
        BigDecimal requested = totalRequested(request.getSelections());

        // checked up front so the whole selection is rejected as one decision, rather than
        // failing partway through and relying on the per-row guard to catch the overdraw
        new ValidationCheck()
                .check(requested.compareTo(remaining) <= 0,
                        "Cannot allocate " + requested + " from disbursement " + disbursement.getId()
                                + ": only " + remaining + " remains")
                .throwIfAny(ConflictException::new);

        List<Funding> created = new ArrayList<>();
        for (AllocationSelection selection : request.getSelections()) {
            created.add(fundingService.create(FundingRequest.builder()
                    .disbursementId(disbursement.getId())
                    .expenseId(selection.getExpenseId())
                    .amountCovered(selection.getAmount())
                    .build()));
        }

        // re-read: the last funding row may have drained the advance and closed it
        Disbursement settled = disbursementService.getById(disbursement.getId());
        return new AllocationResult(
                created,
                fundingService.getRemainingBalance(settled.getId()),
                settled.getStatus());
    }

    private void validateShape(AllocationRequest request) {
        new ValidationCheck()
                .check(request.getDisbursementId() != null, "Disbursement id is required")
                .check(request.getSelections() != null && !request.getSelections().isEmpty(),
                        "At least one expense selection is required")
                .throwIfAny(ValidationException::new);

        ValidationCheck check = new ValidationCheck();
        Set<UUID> seen = new HashSet<>();
        for (AllocationSelection selection : request.getSelections()) {
            check.check(selection.getExpenseId() != null, "Expense id is required for every selection")
                    .check(selection.getAmount() != null
                            && selection.getAmount().compareTo(BigDecimal.ZERO) > 0,
                            "Every selection amount must be greater than 0");
            if (selection.getExpenseId() != null) {
                check.check(seen.add(selection.getExpenseId()),
                        "Expense " + selection.getExpenseId() + " is selected more than once");
            }
        }
        check.throwIfAny(ValidationException::new);
    }

    private BigDecimal totalRequested(List<AllocationSelection> selections) {
        return selections.stream()
                .map(AllocationSelection::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
