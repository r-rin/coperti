package com.github.rrin;

import com.github.rrin.expense.DisbursementStatus;
import com.github.rrin.expense.dto.filter.BudgetFilter;
import com.github.rrin.expense.dto.filter.DisbursementFilter;
import com.github.rrin.expense.repository.BudgetRepository;
import com.github.rrin.expense.service.DisbursementService;
import com.github.rrin.expense.service.implementation.BudgetServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private DisbursementService disbursementService;

    @InjectMocks
    private BudgetServiceImpl service;

    @Captor
    private ArgumentCaptor<DisbursementFilter> filterCaptor;

    @Test
    void facilityFloatIgnoresCancelledDisbursements() {
        when(budgetRepository.sumAmount(any())).thenReturn(new BigDecimal("10000.00"));
        when(disbursementService.sum(any(DisbursementFilter.class))).thenReturn(new BigDecimal("4000.00"));

        BigDecimal remaining = service.getFacilityFloat();

        assertEquals(new BigDecimal("6000.00"), remaining);
        org.mockito.Mockito.verify(disbursementService).sum(filterCaptor.capture());
        assertEquals(EnumSet.of(DisbursementStatus.OPEN, DisbursementStatus.CLOSED),
                filterCaptor.getValue().getStatuses());
    }

    @Test
    void facilityFloatNeverGoesNegative() {
        when(budgetRepository.sumAmount(any())).thenReturn(new BigDecimal("1000.00"));
        when(disbursementService.sum(any(DisbursementFilter.class))).thenReturn(new BigDecimal("4000.00"));

        assertEquals(BigDecimal.ZERO, service.getFacilityFloat());
    }

    @Test
    void sumPassesFilterThrough() {
        when(budgetRepository.sumAmount(any())).thenReturn(new BigDecimal("250.00"));

        assertEquals(new BigDecimal("250.00"), service.sum(BudgetFilter.builder().build()));
    }
}
