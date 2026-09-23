package com.github.rrin.expense.service.implementation;

import com.github.rrin.exception.DateRangeConstraintValidator;
import com.github.rrin.exception.PageConstraintValidator;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.InvalidQuery;
import com.github.rrin.expense.Budget;
import com.github.rrin.expense.DisbursementStatus;
import com.github.rrin.expense.dto.BudgetRequest;
import com.github.rrin.expense.dto.filter.BudgetFilter;
import com.github.rrin.expense.dto.filter.DisbursementFilter;
import com.github.rrin.expense.repository.BudgetRepository;
import com.github.rrin.expense.repository.specs.BudgetSpecs;
import com.github.rrin.expense.service.BudgetService;
import com.github.rrin.expense.service.DisbursementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.UUID;

@Service
public class BudgetServiceImpl implements BudgetService {

    private BudgetRepository budgetRepository;
    private DisbursementService disbursementService;

    @Autowired
    public BudgetServiceImpl(BudgetRepository budgetRepository, DisbursementService disbursementService) {
        this.budgetRepository = budgetRepository;
        this.disbursementService = disbursementService;
    }


    @Override
    public Budget create(BudgetRequest budgetRequest) {
        Budget budget = Budget.builder()
                .amount(budgetRequest.getAmount())
                .givenBy(budgetRequest.getGivenBy())
                .description(budgetRequest.getDescription())
                .date(budgetRequest.getDate())
                .build();
        return budgetRepository.save(budget);
    }

    @Override
    public Budget getById(UUID id) {
        return getIfExists(id);
    }

    @Override
    public BigDecimal getFacilityFloat() {
        BigDecimal totalSum = sum(BudgetFilter.builder().build());
        // CANCELLED advances never left the till, so they are not money spent
        BigDecimal totalSpent = disbursementService.sum(DisbursementFilter.builder()
                .statuses(EnumSet.of(DisbursementStatus.OPEN, DisbursementStatus.CLOSED))
                .build());
        if (totalSum.compareTo(totalSpent) > 0) {
            return totalSum.subtract(totalSpent);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public Page<Budget> search(BudgetFilter filter, int page, int size) {
        new PageConstraintValidator(page, size).throwIfAny(InvalidQuery::new);
        new DateRangeConstraintValidator(filter.getFromDate(), filter.getToDate()).throwIfAny(InvalidQuery::new);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        return budgetRepository.findAll(BudgetSpecs.matching(filter), pageable);
    }

    @Override
    public BigDecimal sum(BudgetFilter filter) {
        new DateRangeConstraintValidator(filter.getFromDate(), filter.getToDate()).throwIfAny(InvalidQuery::new);

        return budgetRepository.sumAmount(BudgetSpecs.matching(filter));
    }

    private Budget getIfExists(UUID id) {
        return budgetRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Budget with id " + id + " not found")
        );
    }
}
