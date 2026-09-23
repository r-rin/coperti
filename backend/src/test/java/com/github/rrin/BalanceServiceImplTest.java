package com.github.rrin;

import com.github.rrin.expense.DisbursementStatus;
import com.github.rrin.expense.repository.DisbursementRepository;
import com.github.rrin.expense.repository.ExpenseRepository;
import com.github.rrin.expense.repository.projection.AdvanceDrawRow;
import com.github.rrin.expense.repository.projection.ExpenseCoverageRow;
import com.github.rrin.expense.service.EmployeeBalance;
import com.github.rrin.expense.service.implementation.BalanceServiceImpl;
import com.github.rrin.identity.Employee;
import com.github.rrin.identity.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private DisbursementRepository disbursementRepository;

    @InjectMocks
    private BalanceServiceImpl service;

    private final Employee busy = Employee.builder().id(UUID.randomUUID()).name("Busy").build();
    private final Employee idle = Employee.builder().id(UUID.randomUUID()).name("Idle").build();

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    @Test
    void derivesEveryFigurePerEmployee() {
        when(employeeRepository.findAll()).thenReturn(List.of(busy, idle));
        when(expenseRepository.findCoverageRows()).thenReturn(List.of(
                new ExpenseCoverageRow(busy.getId(), money("100.00"), money("30.00")),
                // edited below its funding: counts as fully covered, and must not offset the other debt
                new ExpenseCoverageRow(busy.getId(), money("50.00"), money("80.00"))));
        when(disbursementRepository.findAdvanceDrawRows()).thenReturn(List.of(
                new AdvanceDrawRow(busy.getId(), DisbursementStatus.OPEN, money("200.00"), money("80.00")),
                new AdvanceDrawRow(busy.getId(), DisbursementStatus.CLOSED, money("100.00"), money("60.00")),
                new AdvanceDrawRow(busy.getId(), DisbursementStatus.CANCELLED, money("500.00"), money("0"))));

        List<EmployeeBalance> balances = service.getEmployeeBalances();

        EmployeeBalance b = balances.get(0);
        assertEquals(busy, b.employee());
        assertEquals(money("150.00"), b.spent());
        assertEquals(money("80.00"), b.covered());
        assertEquals(money("70.00"), b.owed());
        // cancelled advances never left the till
        assertEquals(money("300.00"), b.advanced());
        // a closed advance's remainder was written off, so only the open one leaves cash in hand
        assertEquals(money("120.00"), b.unspent());
    }

    @Test
    void employeesWithNothingRecordedStillGetAZeroBalance() {
        when(employeeRepository.findAll()).thenReturn(List.of(idle));
        when(expenseRepository.findCoverageRows()).thenReturn(List.of());
        when(disbursementRepository.findAdvanceDrawRows()).thenReturn(List.of());

        EmployeeBalance b = service.getEmployeeBalances().get(0);

        assertEquals(idle, b.employee());
        assertEquals(BigDecimal.ZERO, b.owed());
        assertEquals(BigDecimal.ZERO, b.unspent());
    }
}
