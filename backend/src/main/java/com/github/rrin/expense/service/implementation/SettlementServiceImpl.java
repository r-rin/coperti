package com.github.rrin.expense.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.expense.Disbursement;
import com.github.rrin.expense.Expense;
import com.github.rrin.expense.Funding;
import com.github.rrin.expense.dto.DisbursementRequest;
import com.github.rrin.expense.dto.FundingRequest;
import com.github.rrin.expense.dto.SettlementRequest;
import com.github.rrin.expense.service.DisbursementService;
import com.github.rrin.expense.service.ExpenseService;
import com.github.rrin.expense.service.FundingService;
import com.github.rrin.expense.service.SettlementResult;
import com.github.rrin.expense.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class SettlementServiceImpl implements SettlementService {

    private static final Comparator<Expense> OLDEST_FIRST = Comparator
            .comparing(Expense::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Expense::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private final FundingService fundingService;
    private final DisbursementService disbursementService;
    private final ExpenseService expenseService;

    @Autowired
    public SettlementServiceImpl(FundingService fundingService,
                                 DisbursementService disbursementService,
                                 ExpenseService expenseService) {
        this.fundingService = fundingService;
        this.disbursementService = disbursementService;
        this.expenseService = expenseService;
    }

    @Override
    @Transactional
    public SettlementResult settle(SettlementRequest request) {
        validateShape(request);

        List<Expense> expenses = request.getExpenseIds().stream()
                .map(expenseService::get)
                .sorted(OLDEST_FIRST)
                .toList();

        new ValidationCheck()
                .check(expenses.stream().allMatch(e -> request.getEmployeeId().equals(e.getEmployee().getId())),
                        "Every selected expense must belong to employee " + request.getEmployeeId())
                .throwIfAny(ValidationException::new);

        // the working copy of each expense's debt, drawn down as funding rows are written
        Map<UUID, BigDecimal> owed = new HashMap<>();
        ValidationCheck coverable = new ValidationCheck();
        for (Expense expense : expenses) {
            BigDecimal shortfall = fundingService.getShortfall(expense.getId());
            owed.put(expense.getId(), shortfall);
            coverable.check(shortfall.signum() > 0, "Expense " + expense.getId() + " is already fully covered");
        }
        coverable.throwIfAny(ConflictException::new);

        List<Funding> created = new ArrayList<>();

        BigDecimal fromAdvances = BigDecimal.ZERO;
        for (Disbursement advance : disbursementService.getOpenFor(request.getEmployeeId())) {
            if (totalOf(owed).signum() == 0) break;
            BigDecimal available = fundingService.getRemainingBalance(advance.getId());
            fromAdvances = fromAdvances.add(drawInto(advance, available, expenses, owed, created));
        }

        Disbursement payout = null;
        BigDecimal fromNewCash = BigDecimal.ZERO;
        if (request.getAmount().signum() > 0) {
            // create() always opens the advance; drawing it to zero below closes it through the guarded path
            payout = disbursementService.create(DisbursementRequest.builder()
                    .employeeId(request.getEmployeeId())
                    .amount(request.getAmount())
                    .date(request.getDate() != null ? request.getDate() : LocalDate.now())
                    .build());
            fromNewCash = drawInto(payout, request.getAmount(), expenses, owed, created);
        }

        new ValidationCheck()
                .check(!created.isEmpty(),
                        "Nothing to settle with: employee " + request.getEmployeeId()
                                + " holds no unspent advance and no new cash was given")
                .throwIfAny(ConflictException::new);

        return new SettlementResult(
                created,
                fromAdvances,
                fromNewCash,
                payout,
                request.getAmount().subtract(fromNewCash),
                totalOf(owed));
    }

    /** Covers the expenses in order from one source until either runs dry; returns what was drawn. */
    private BigDecimal drawInto(Disbursement source,
                                BigDecimal available,
                                List<Expense> ordered,
                                Map<UUID, BigDecimal> owed,
                                List<Funding> created) {
        BigDecimal drawn = BigDecimal.ZERO;
        for (Expense expense : ordered) {
            BigDecimal room = available.subtract(drawn);
            if (room.signum() <= 0) break;
            BigDecimal need = owed.get(expense.getId());
            if (need.signum() <= 0) continue;

            BigDecimal take = room.min(need);
            created.add(fundingService.create(FundingRequest.builder()
                    .disbursementId(source.getId())
                    .expenseId(expense.getId())
                    .amountCovered(take)
                    .build()));
            owed.put(expense.getId(), need.subtract(take));
            drawn = drawn.add(take);
        }
        return drawn;
    }

    private void validateShape(SettlementRequest request) {
        new ValidationCheck()
                .check(request.getEmployeeId() != null, "Employee id is required")
                .check(request.getAmount() != null && request.getAmount().signum() >= 0,
                        "Amount must be 0 or greater")
                .check(request.getExpenseIds() != null && !request.getExpenseIds().isEmpty(),
                        "At least one expense is required")
                .throwIfAny(ValidationException::new);

        ValidationCheck check = new ValidationCheck();
        Set<UUID> seen = new HashSet<>();
        for (UUID expenseId : request.getExpenseIds()) {
            check.check(expenseId != null, "Expense ids must not be empty");
            if (expenseId != null) {
                check.check(seen.add(expenseId), "Expense " + expenseId + " is selected more than once");
            }
        }
        check.throwIfAny(ValidationException::new);
    }

    private static BigDecimal totalOf(Map<UUID, BigDecimal> owed) {
        return owed.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
