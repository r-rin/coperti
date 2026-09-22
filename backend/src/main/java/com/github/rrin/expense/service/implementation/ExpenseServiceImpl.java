package com.github.rrin.expense.service.implementation;

import com.github.rrin.exception.DateRangeConstraintValidator;
import com.github.rrin.exception.PageConstraintsValidator;
import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.expense.Expense;
import com.github.rrin.expense.dto.ExpenseRequest;
import com.github.rrin.expense.dto.filter.ExpenseFilter;
import com.github.rrin.expense.repository.ExpenseRepository;
import com.github.rrin.expense.repository.specs.ExpenseSpecs;
import com.github.rrin.expense.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Autowired
    public ExpenseServiceImpl(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public Expense create(ExpenseRequest request) {
        new ValidationCheck()
                .check(request.getAmount() != null && request.getAmount().doubleValue() > 0, "Amount must be greater than 0")
                .throwIfAny(ValidationException::new);

        Expense expense = Expense.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .date(request.getDate())
                .build();

        return expenseRepository.save(expense);
    }

    @Override
    public Expense update(ExpenseRequest request) {
        Expense expense = getIfExists(request.getId());

        new ValidationCheck()
                .check(request.getAmount() != null && request.getAmount().doubleValue() > 0, "Amount must be greater than 0")
                .throwIfAny(ValidationException::new);

        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setDate(request.getDate());

        return expenseRepository.save(expense);
    }

    @Override
    public Expense get(UUID id) {
        return getIfExists(id);
    }

    @Override
    public Expense delete(UUID id) {
        Expense expense = getIfExists(id);
        expenseRepository.delete(expense);
        return expense;
    }

    @Override
    public Page<Expense> search(ExpenseFilter filter, int page, int size) {
        new PageConstraintsValidator(page, size)
                .throwIfAny(ValidationException::new);
        new DateRangeConstraintValidator(filter.getFromDate(), filter.getToDate())
                .throwIfAny(ValidationException::new);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        return expenseRepository.findAll(ExpenseSpecs.matching(filter), pageable);
    }

    private Expense getIfExists(UUID id){
        return expenseRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Expense with id " + id + " does not exist")
        );
    }
}
