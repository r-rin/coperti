package com.github.rrin.telegram.controller;

import com.github.rrin.telegram.dto.LinkCodeResponse;
import com.github.rrin.telegram.dto.TelegramAccountRequest;
import com.github.rrin.telegram.dto.TelegramAccountResponse;
import com.github.rrin.telegram.service.TelegramAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Administration of employee ↔ Telegram links (open like the rest of the API until users can log in). */
@RestController
@RequestMapping("/api/telegram-account")
public class TelegramAccountController {

    private final TelegramAccountService accountService;
    private final TelegramMapper mapper;

    @Autowired
    public TelegramAccountController(TelegramAccountService accountService, TelegramMapper mapper) {
        this.accountService = accountService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<TelegramAccountResponse> getAll() {
        return accountService.getAll().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/employee/{employeeId}")
    public TelegramAccountResponse get(@PathVariable UUID employeeId) {
        return mapper.toResponse(accountService.getForEmployee(employeeId));
    }

    @PutMapping
    public TelegramAccountResponse link(@RequestBody TelegramAccountRequest request) {
        return mapper.toResponse(accountService.link(request));
    }

    @DeleteMapping("/employee/{employeeId}")
    public TelegramAccountResponse unlink(@PathVariable UUID employeeId) {
        return mapper.toResponse(accountService.unlink(employeeId));
    }

    @PostMapping("/employee/{employeeId}/link-code")
    public LinkCodeResponse issueLinkCode(@PathVariable UUID employeeId) {
        return mapper.toResponse(accountService.issueLinkCode(employeeId));
    }
}
