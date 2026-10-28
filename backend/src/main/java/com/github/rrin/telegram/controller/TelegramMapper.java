package com.github.rrin.telegram.controller;

import com.github.rrin.identity.Employee;
import com.github.rrin.identity.dto.EmployeeResponse;
import com.github.rrin.telegram.TelegramAccount;
import com.github.rrin.telegram.TelegramLinkCode;
import com.github.rrin.telegram.dto.LinkCodeResponse;
import com.github.rrin.telegram.dto.TelegramAccountResponse;
import org.springframework.stereotype.Component;

@Component
public class TelegramMapper {

    public TelegramAccountResponse toResponse(TelegramAccount account) {
        return TelegramAccountResponse.builder()
                .id(account.getId())
                .employeeId(account.getEmployee().getId())
                .telegramUserId(account.getTelegramUserId())
                .linkedAt(account.getLinkedAt())
                .build();
    }

    public LinkCodeResponse toResponse(TelegramLinkCode code) {
        return LinkCodeResponse.builder()
                .code(code.getCode())
                .employeeId(code.getEmployee().getId())
                .expiresAt(code.getExpiresAt())
                .build();
    }

    public EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .build();
    }
}
