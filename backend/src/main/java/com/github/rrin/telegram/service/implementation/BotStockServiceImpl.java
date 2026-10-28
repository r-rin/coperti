package com.github.rrin.telegram.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.identity.Employee;
import com.github.rrin.inventory.Location;
import com.github.rrin.inventory.LocationKind;
import com.github.rrin.inventory.MovementType;
import com.github.rrin.inventory.StockMovement;
import com.github.rrin.inventory.dto.StockMovementRequest;
import com.github.rrin.inventory.service.LocationService;
import com.github.rrin.inventory.service.StockMovementService;
import com.github.rrin.telegram.dto.BotStockRequest;
import com.github.rrin.telegram.service.BotStockService;
import com.github.rrin.telegram.service.TelegramAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class BotStockServiceImpl implements BotStockService {

    static final String NOTE = "via Telegram";

    private final TelegramAccountService accountService;
    private final LocationService locationService;
    private final StockMovementService movementService;

    @Autowired
    public BotStockServiceImpl(TelegramAccountService accountService,
                               LocationService locationService,
                               StockMovementService movementService) {
        this.accountService = accountService;
        this.locationService = locationService;
        this.movementService = movementService;
    }

    @Override
    @Transactional
    public StockMovement receive(long telegramUserId, BotStockRequest request) {
        Employee actor = accountService.resolve(telegramUserId);
        validate(request, false);
        Location warehouse = requireWarehouse(request.getWarehouseId());

        return movementService.post(StockMovementRequest.builder()
                .itemId(request.getItemId())
                .qty(request.getQty())
                .fromLocationId(locationService.getVirtual(LocationKind.V_SUPPLIER).getId())
                .toLocationId(warehouse.getId())
                .movementType(MovementType.RECEIPT)
                .performedById(actor.getId())
                .note(NOTE)
                .build());
    }

    @Override
    @Transactional
    public StockMovement give(long telegramUserId, BotStockRequest request) {
        Employee actor = accountService.resolve(telegramUserId);
        validate(request, true);
        Location warehouse = requireWarehouse(request.getWarehouseId());

        return movementService.post(StockMovementRequest.builder()
                .itemId(request.getItemId())
                .qty(request.getQty())
                .fromLocationId(warehouse.getId())
                .toLocationId(locationService.getForEmployee(request.getEmployeeId()).getId())
                .movementType(MovementType.ISSUE_TO_EMPLOYEE)
                .performedById(actor.getId())
                .note(NOTE)
                .build());
    }

    private void validate(BotStockRequest request, boolean needsRecipient) {
        new ValidationCheck()
                .check(request.getItemId() != null, "Item id is required")
                .check(request.getWarehouseId() != null, "Warehouse id is required")
                .check(!needsRecipient || request.getEmployeeId() != null, "Employee id is required")
                .check(request.getQty() != null && request.getQty().compareTo(BigDecimal.ZERO) > 0,
                        "Quantity must be greater than 0")
                .throwIfAny(ValidationException::new);
    }

    // the bot only works with warehouses; benches, quarantine and the like are moved through the web app
    private Location requireWarehouse(UUID locationId) {
        Location location = locationService.get(locationId);
        new ValidationCheck()
                .check(location.getKind() == LocationKind.WAREHOUSE, "Location " + location.getCode() + " is not a warehouse")
                .throwIfAny(ValidationException::new);
        return location;
    }
}
