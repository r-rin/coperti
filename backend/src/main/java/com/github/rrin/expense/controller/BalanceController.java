package com.github.rrin.expense.controller;

import com.github.rrin.expense.dto.EmployeeBalanceResponse;
import com.github.rrin.expense.service.BalanceService;
import com.github.rrin.expense.service.EmployeeBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/balance")
public class BalanceController {

    private final BalanceService balanceService;

    @Autowired
    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping("/employee")
    public List<EmployeeBalanceResponse> byEmployee() {
        return balanceService.getEmployeeBalances().stream().map(this::toResponse).toList();
    }

    private EmployeeBalanceResponse toResponse(EmployeeBalance balance) {
        return EmployeeBalanceResponse.builder()
                .employeeId(balance.employee().getId())
                .employeeName(balance.employee().getName())
                .spent(balance.spent())
                .covered(balance.covered())
                .owed(balance.owed())
                .advanced(balance.advanced())
                .unspent(balance.unspent())
                .build();
    }
}
