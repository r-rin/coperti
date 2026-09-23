package com.github.rrin;

import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.expense.Disbursement;
import com.github.rrin.expense.DisbursementStatus;
import com.github.rrin.expense.Expense;
import com.github.rrin.expense.Funding;
import com.github.rrin.expense.dto.DisbursementRequest;
import com.github.rrin.expense.dto.FundingRequest;
import com.github.rrin.expense.dto.SettlementRequest;
import com.github.rrin.expense.service.DisbursementService;
import com.github.rrin.expense.service.ExpenseService;
import com.github.rrin.expense.service.FundingService;
import com.github.rrin.expense.service.SettlementResult;
import com.github.rrin.expense.service.implementation.SettlementServiceImpl;
import com.github.rrin.identity.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementServiceImplTest {

    @Mock
    private FundingService fundingService;

    @Mock
    private DisbursementService disbursementService;

    @Mock
    private ExpenseService expenseService;

    @InjectMocks
    private SettlementServiceImpl service;

    @Captor
    private ArgumentCaptor<FundingRequest> fundingCaptor;

    @Captor
    private ArgumentCaptor<DisbursementRequest> disbursementCaptor;

    private final Employee worker = Employee.builder().id(UUID.randomUUID()).name("Worker").build();

    // older receipt, deliberately passed second so the test proves the service sorts by date
    private final Expense older = expense("2026-09-01");
    private final Expense newer = expense("2026-09-05");

    private final Disbursement heldAdvance = advance(UUID.randomUUID());
    private final Disbursement payout = advance(UUID.randomUUID());

    private Expense expense(String date) {
        return Expense.builder()
                .id(UUID.randomUUID())
                .employee(worker)
                .amount(new BigDecimal("999.00"))
                .date(LocalDate.parse(date))
                .build();
    }

    private Disbursement advance(UUID id) {
        return Disbursement.builder().id(id).employee(worker).status(DisbursementStatus.OPEN).build();
    }

    private SettlementRequest request(String amount, Expense... expenses) {
        return SettlementRequest.builder()
                .employeeId(worker.getId())
                .amount(new BigDecimal(amount))
                .date(LocalDate.parse("2026-09-20"))
                .expenseIds(java.util.Arrays.stream(expenses).map(Expense::getId).toList())
                .build();
    }

    private void owes(Expense expense, String shortfall) {
        when(expenseService.get(expense.getId())).thenReturn(expense);
        when(fundingService.getShortfall(expense.getId())).thenReturn(new BigDecimal(shortfall));
    }

    private void assertFunding(int index, Disbursement source, Expense expense, String amount) {
        FundingRequest funding = fundingCaptor.getAllValues().get(index);
        assertEquals(source.getId(), funding.getDisbursementId());
        assertEquals(expense.getId(), funding.getExpenseId());
        assertEquals(new BigDecimal(amount), funding.getAmountCovered());
    }

    @Test
    void spendsTheHeldAdvanceFirstThenNewCashOldestExpenseFirst() {
        owes(older, "100.00");
        owes(newer, "200.00");
        when(disbursementService.getOpenFor(worker.getId())).thenReturn(List.of(heldAdvance));
        when(fundingService.getRemainingBalance(heldAdvance.getId())).thenReturn(new BigDecimal("150.00"));
        when(disbursementService.create(any(DisbursementRequest.class))).thenReturn(payout);
        when(fundingService.create(any(FundingRequest.class))).thenReturn(Funding.builder().build());

        SettlementResult result = service.settle(request("300.00", newer, older));

        verify(fundingService, times(3)).create(fundingCaptor.capture());
        assertFunding(0, heldAdvance, older, "100.00");
        assertFunding(1, heldAdvance, newer, "50.00");
        assertFunding(2, payout, newer, "150.00");

        assertEquals(new BigDecimal("150.00"), result.fromAdvances());
        assertEquals(new BigDecimal("150.00"), result.fromNewCash());
        // new cash beyond the selection's debt stays with the worker on the open payout advance
        assertEquals(new BigDecimal("150.00"), result.leftOnNewAdvance());
        assertEquals(new BigDecimal("0.00"), result.stillOwed());
        assertSame(payout, result.newDisbursement());

        verify(disbursementService).create(disbursementCaptor.capture());
        assertEquals(worker.getId(), disbursementCaptor.getValue().getEmployeeId());
        assertEquals(new BigDecimal("300.00"), disbursementCaptor.getValue().getAmount());
    }

    @Test
    void underpaymentLeavesTheNewestExpensePartlyOwed() {
        owes(older, "100.00");
        owes(newer, "200.00");
        when(disbursementService.getOpenFor(worker.getId())).thenReturn(List.of());
        when(disbursementService.create(any(DisbursementRequest.class))).thenReturn(payout);
        when(fundingService.create(any(FundingRequest.class))).thenReturn(Funding.builder().build());

        SettlementResult result = service.settle(request("150.00", older, newer));

        verify(fundingService, times(2)).create(fundingCaptor.capture());
        assertFunding(0, payout, older, "100.00");
        assertFunding(1, payout, newer, "50.00");
        assertEquals(new BigDecimal("150.00"), result.stillOwed());
        assertEquals(new BigDecimal("0.00"), result.leftOnNewAdvance());
    }

    @Test
    void heldAdvanceAloneCanSettleWithoutNewCash() {
        owes(older, "100.00");
        when(disbursementService.getOpenFor(worker.getId())).thenReturn(List.of(heldAdvance));
        when(fundingService.getRemainingBalance(heldAdvance.getId())).thenReturn(new BigDecimal("500.00"));
        when(fundingService.create(any(FundingRequest.class))).thenReturn(Funding.builder().build());

        SettlementResult result = service.settle(request("0", older));

        assertEquals(new BigDecimal("100.00"), result.fromAdvances());
        assertNull(result.newDisbursement());
        verify(disbursementService, never()).create(any(DisbursementRequest.class));
    }

    @Test
    void rejectsAnotherEmployeesExpense() {
        Expense someoneElses = Expense.builder()
                .id(UUID.randomUUID())
                .employee(Employee.builder().id(UUID.randomUUID()).build())
                .build();
        when(expenseService.get(someoneElses.getId())).thenReturn(someoneElses);

        assertThrows(ValidationException.class, () -> service.settle(request("50.00", someoneElses)));
        verify(fundingService, never()).create(any(FundingRequest.class));
    }

    @Test
    void rejectsAnAlreadyCoveredExpense() {
        owes(older, "0.00");

        assertThrows(ConflictException.class, () -> service.settle(request("50.00", older)));
        verify(fundingService, never()).create(any(FundingRequest.class));
    }

    @Test
    void rejectsWhenThereIsNothingToPayWith() {
        owes(older, "100.00");
        when(disbursementService.getOpenFor(worker.getId())).thenReturn(List.of());

        assertThrows(ConflictException.class, () -> service.settle(request("0", older)));
    }

    @Test
    void rejectsANegativeAmount() {
        assertThrows(ValidationException.class, () -> service.settle(request("-1.00", older)));
    }

    @Test
    void rejectsTheSameExpenseTwice() {
        assertThrows(ValidationException.class, () -> service.settle(request("10.00", older, older)));
    }
}
