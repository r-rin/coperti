package com.github.rrin.expense.service.implementation;

import com.github.rrin.expense.DisbursementStatus;
import com.github.rrin.expense.repository.DisbursementRepository;
import com.github.rrin.expense.repository.ExpenseRepository;
import com.github.rrin.expense.repository.projection.AdvanceDrawRow;
import com.github.rrin.expense.repository.projection.ExpenseCoverageRow;
import com.github.rrin.expense.service.BalanceService;
import com.github.rrin.expense.service.EmployeeBalance;
import com.github.rrin.identity.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BalanceServiceImpl implements BalanceService {

    private final EmployeeRepository employeeRepository;
    private final ExpenseRepository expenseRepository;
    private final DisbursementRepository disbursementRepository;

    @Autowired
    public BalanceServiceImpl(EmployeeRepository employeeRepository,
                              ExpenseRepository expenseRepository,
                              DisbursementRepository disbursementRepository) {
        this.employeeRepository = employeeRepository;
        this.expenseRepository = expenseRepository;
        this.disbursementRepository = disbursementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeBalance> getEmployeeBalances() {
        Map<UUID, Totals> totals = new HashMap<>();

        for (ExpenseCoverageRow row : expenseRepository.findCoverageRows()) {
            BigDecimal amount = orZero(row.amount());
            // clamped per expense, matching FundingService.getShortfall
            BigDecimal covered = orZero(row.covered()).min(amount);
            Totals t = totals.computeIfAbsent(row.employeeId(), id -> new Totals());
            t.spent = t.spent.add(amount);
            t.covered = t.covered.add(covered);
            t.owed = t.owed.add(amount.subtract(covered));
        }

        for (AdvanceDrawRow row : disbursementRepository.findAdvanceDrawRows()) {
            if (row.status() == DisbursementStatus.CANCELLED) continue;
            BigDecimal amount = orZero(row.amount());
            Totals t = totals.computeIfAbsent(row.employeeId(), id -> new Totals());
            t.advanced = t.advanced.add(amount);
            // a CLOSED advance's remainder was written off, so only OPEN ones leave cash in hand
            if (row.status() == DisbursementStatus.OPEN) {
                t.unspent = t.unspent.add(amount.subtract(orZero(row.drawn())).max(BigDecimal.ZERO));
            }
        }

        return employeeRepository.findAll().stream()
                .map(employee -> {
                    Totals t = totals.getOrDefault(employee.getId(), new Totals());
                    return new EmployeeBalance(employee, t.spent, t.covered, t.owed, t.advanced, t.unspent);
                })
                .toList();
    }

    private static BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static final class Totals {
        BigDecimal spent = BigDecimal.ZERO;
        BigDecimal covered = BigDecimal.ZERO;
        BigDecimal owed = BigDecimal.ZERO;
        BigDecimal advanced = BigDecimal.ZERO;
        BigDecimal unspent = BigDecimal.ZERO;
    }
}
