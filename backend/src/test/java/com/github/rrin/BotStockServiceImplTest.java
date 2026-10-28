package com.github.rrin;

import com.github.rrin.exception.types.ForbiddenException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.identity.Employee;
import com.github.rrin.inventory.Location;
import com.github.rrin.inventory.LocationKind;
import com.github.rrin.inventory.MovementType;
import com.github.rrin.inventory.dto.StockMovementRequest;
import com.github.rrin.inventory.service.LocationService;
import com.github.rrin.inventory.service.StockMovementService;
import com.github.rrin.telegram.dto.BotStockRequest;
import com.github.rrin.telegram.service.TelegramAccountService;
import com.github.rrin.telegram.service.implementation.BotStockServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BotStockServiceImplTest {

    @Mock
    private TelegramAccountService accountService;

    @Mock
    private LocationService locationService;

    @Mock
    private StockMovementService movementService;

    @InjectMocks
    private BotStockServiceImpl service;

    private final Employee actor = Employee.builder().id(UUID.randomUUID()).name("Ann").build();
    private final Location warehouse = Location.builder().id(UUID.randomUUID()).code("W1").kind(LocationKind.WAREHOUSE).build();
    private final Location bench = Location.builder().id(UUID.randomUUID()).code("B1").kind(LocationKind.WIP).build();
    private final Location supplier = Location.builder().id(UUID.randomUUID()).kind(LocationKind.V_SUPPLIER).build();
    private final Location hands = Location.builder().id(UUID.randomUUID()).kind(LocationKind.EMPLOYEE).build();
    private final UUID item = UUID.randomUUID();
    private final UUID recipient = UUID.randomUUID();

    private BotStockRequest request(Location location) {
        return BotStockRequest.builder().itemId(item).warehouseId(location.getId()).employeeId(recipient)
                .qty(BigDecimal.TEN).build();
    }

    private StockMovementRequest posted() {
        ArgumentCaptor<StockMovementRequest> captor = ArgumentCaptor.forClass(StockMovementRequest.class);
        verify(movementService).post(captor.capture());
        return captor.getValue();
    }

    @Test
    void receivePostsReceiptAsTheLinkedEmployee() {
        when(accountService.resolve(42L)).thenReturn(actor);
        when(locationService.get(warehouse.getId())).thenReturn(warehouse);
        when(locationService.getVirtual(LocationKind.V_SUPPLIER)).thenReturn(supplier);

        service.receive(42L, request(warehouse));

        StockMovementRequest movement = posted();
        assertEquals(MovementType.RECEIPT, movement.getMovementType());
        assertEquals(supplier.getId(), movement.getFromLocationId());
        assertEquals(warehouse.getId(), movement.getToLocationId());
        assertEquals(actor.getId(), movement.getPerformedById());
    }

    @Test
    void givePostsIssueIntoRecipientLocation() {
        when(accountService.resolve(42L)).thenReturn(actor);
        when(locationService.get(warehouse.getId())).thenReturn(warehouse);
        when(locationService.getForEmployee(recipient)).thenReturn(hands);

        service.give(42L, request(warehouse));

        StockMovementRequest movement = posted();
        assertEquals(MovementType.ISSUE_TO_EMPLOYEE, movement.getMovementType());
        assertEquals(warehouse.getId(), movement.getFromLocationId());
        assertEquals(hands.getId(), movement.getToLocationId());
        assertEquals(actor.getId(), movement.getPerformedById());
    }

    @Test
    void unlinkedTelegramUserCannotAct() {
        when(accountService.resolve(42L)).thenThrow(new ForbiddenException("not linked"));
        assertThrows(ForbiddenException.class, () -> service.receive(42L, request(warehouse)));
        verify(movementService, never()).post(any());
    }

    @Test
    void onlyWarehousesAreAccepted() {
        when(accountService.resolve(42L)).thenReturn(actor);
        when(locationService.get(bench.getId())).thenReturn(bench);
        assertThrows(ValidationException.class, () -> service.give(42L, request(bench)));
        verify(movementService, never()).post(any());
    }

    @Test
    void giveNeedsRecipient() {
        when(accountService.resolve(42L)).thenReturn(actor);
        BotStockRequest request = request(warehouse);
        request.setEmployeeId(null);
        assertThrows(ValidationException.class, () -> service.give(42L, request));
    }
}
