package com.github.rrin.telegram.controller;

import com.github.rrin.identity.dto.EmployeeResponse;
import com.github.rrin.inventory.controller.InventoryMapper;
import com.github.rrin.inventory.dto.StockMovementResponse;
import com.github.rrin.telegram.dto.BotLinkRequest;
import com.github.rrin.telegram.dto.BotStockRequest;
import com.github.rrin.telegram.service.BotStockService;
import com.github.rrin.telegram.service.TelegramAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints for the Telegram bot only, guarded by {@code BotTokenFilter}. The caller is identified by the
 * Telegram user id the bot forwards; the backend maps it to an employee itself.
 */
@RestController
@RequestMapping("/api/bot")
public class BotController {

    public static final String TELEGRAM_USER_HEADER = "X-Telegram-User-Id";

    private final TelegramAccountService accountService;
    private final BotStockService stockService;
    private final TelegramMapper mapper;
    private final InventoryMapper inventoryMapper;

    @Autowired
    public BotController(TelegramAccountService accountService,
                         BotStockService stockService,
                         TelegramMapper mapper,
                         InventoryMapper inventoryMapper) {
        this.accountService = accountService;
        this.stockService = stockService;
        this.mapper = mapper;
        this.inventoryMapper = inventoryMapper;
    }

    @GetMapping("/me")
    public EmployeeResponse me(@RequestHeader(TELEGRAM_USER_HEADER) long telegramUserId) {
        return mapper.toResponse(accountService.resolve(telegramUserId));
    }

    @PostMapping("/link")
    public EmployeeResponse link(@RequestHeader(TELEGRAM_USER_HEADER) long telegramUserId,
                                 @RequestBody BotLinkRequest request) {
        return mapper.toResponse(accountService.redeemLinkCode(request.getCode(), telegramUserId).getEmployee());
    }

    @PostMapping("/receive")
    public StockMovementResponse receive(@RequestHeader(TELEGRAM_USER_HEADER) long telegramUserId,
                                         @RequestBody BotStockRequest request) {
        return inventoryMapper.toResponse(stockService.receive(telegramUserId, request));
    }

    @PostMapping("/give")
    public StockMovementResponse give(@RequestHeader(TELEGRAM_USER_HEADER) long telegramUserId,
                                      @RequestBody BotStockRequest request) {
        return inventoryMapper.toResponse(stockService.give(telegramUserId, request));
    }
}
