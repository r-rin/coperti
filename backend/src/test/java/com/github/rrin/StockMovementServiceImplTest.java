package com.github.rrin;

import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.identity.repository.EmployeeRepository;
import com.github.rrin.inventory.*;
import com.github.rrin.inventory.dto.StockMovementRequest;
import com.github.rrin.inventory.dto.StockReversalRequest;
import com.github.rrin.inventory.repository.LocationRepository;
import com.github.rrin.inventory.repository.OtherUseRepository;
import com.github.rrin.inventory.repository.StockMovementRepository;
import com.github.rrin.inventory.service.StockBalanceService;
import com.github.rrin.inventory.service.implementation.StockMovementServiceImpl;
import com.github.rrin.item.Item;
import com.github.rrin.item.repository.ItemRepository;
import com.github.rrin.production.WorkOrder;
import com.github.rrin.production.repository.WorkOrderRepository;
import com.github.rrin.production.repository.WorkOrderStepRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceImplTest {

    @Mock
    private StockMovementRepository movementRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private WorkOrderStepRepository stepRepository;

    @Mock
    private OtherUseRepository otherUseRepository;

    @Mock
    private StockBalanceService balanceService;

    @InjectMocks
    private StockMovementServiceImpl service;

    private final Item screw = Item.builder().id(UUID.randomUUID()).name("screw").build();
    private final Location supplier = location(LocationKind.V_SUPPLIER);
    private final Location warehouse = location(LocationKind.WAREHOUSE);
    private final Location bench = location(LocationKind.WIP);

    private static Location location(LocationKind kind) {
        return Location.builder().id(UUID.randomUUID()).code(kind.name()).kind(kind).build();
    }

    private void stubLocations(Location... locations) {
        for (Location location : locations) {
            when(locationRepository.findById(location.getId())).thenReturn(Optional.of(location));
        }
    }

    private StockMovementRequest request(MovementType type, Location from, Location to) {
        return StockMovementRequest.builder().itemId(screw.getId()).qty(BigDecimal.TEN)
                .fromLocationId(from.getId()).toLocationId(to.getId()).movementType(type).build();
    }

    @Test
    void receiptIsSavedAndAppliedToBalances() {
        stubLocations(supplier, warehouse);
        when(itemRepository.findById(screw.getId())).thenReturn(Optional.of(screw));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        StockMovement saved = service.post(request(MovementType.RECEIPT, supplier, warehouse));

        assertEquals(QualityState.GOOD, saved.getFromQuality());
        assertEquals(QualityState.GOOD, saved.getToQuality());
        assertNotNull(saved.getOccurredAt());
        verify(balanceService).apply(saved);
    }

    @Test
    void receiptMustComeFromSupplier() {
        stubLocations(warehouse, bench);
        assertThrows(ValidationException.class, () -> service.post(request(MovementType.RECEIPT, warehouse, bench)));
        verify(movementRepository, never()).save(any());
    }

    @Test
    void movementMustChangeSomething() {
        stubLocations(warehouse);
        assertThrows(ValidationException.class, () -> service.post(request(MovementType.TRANSFER, warehouse, warehouse)));
    }

    @Test
    void qualityChangeHappensInPlace() {
        stubLocations(warehouse);
        when(itemRepository.findById(screw.getId())).thenReturn(Optional.of(screw));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        StockMovementRequest request = request(MovementType.QUALITY_CHANGE, warehouse, warehouse);
        request.setToQuality(QualityState.DEFECTIVE);

        assertEquals(QualityState.DEFECTIVE, service.post(request).getToQuality());
    }

    @Test
    void postRejectsTypesOwnedByOtherServices() {
        assertThrows(ValidationException.class, () -> service.post(request(MovementType.CONSUME, warehouse, bench)));
    }

    @Test
    void postRejectsWorkOrderLinks() {
        StockMovementRequest request = request(MovementType.TRANSFER, warehouse, bench);
        request.setWorkOrderId(UUID.randomUUID());
        assertThrows(ValidationException.class, () -> service.post(request));
    }

    @Test
    void recordAcceptsOwnedTypes() {
        Location consumed = location(LocationKind.V_CONSUMED);
        stubLocations(bench, consumed);
        when(itemRepository.findById(screw.getId())).thenReturn(Optional.of(screw));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals(MovementType.CONSUME, service.record(request(MovementType.CONSUME, bench, consumed)).getMovementType());
    }

    private StockMovement transfer() {
        return StockMovement.builder().id(UUID.randomUUID()).item(screw).qty(BigDecimal.TEN)
                .fromLocation(warehouse).toLocation(bench)
                .fromQuality(QualityState.GOOD).toQuality(QualityState.GOOD)
                .movementType(MovementType.TRANSFER).occurredAt(Instant.EPOCH).build();
    }

    @Test
    void reverseMirrorsTheOriginal() {
        StockMovement original = transfer();
        when(movementRepository.findById(original.getId())).thenReturn(Optional.of(original));
        when(movementRepository.existsByReversalOf_Id(original.getId())).thenReturn(false);
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        StockMovement reversal = service.reverse(original.getId(), new StockReversalRequest(null, "typo"));

        assertEquals(bench, reversal.getFromLocation());
        assertEquals(warehouse, reversal.getToLocation());
        assertEquals(original, reversal.getReversalOf());
        verify(balanceService).apply(reversal);
    }

    @Test
    void reverseOnlyOnce() {
        StockMovement original = transfer();
        when(movementRepository.findById(original.getId())).thenReturn(Optional.of(original));
        when(movementRepository.existsByReversalOf_Id(original.getId())).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.reverse(original.getId(), new StockReversalRequest()));
    }

    @Test
    void reversalCannotBeReversed() {
        StockMovement reversal = transfer();
        reversal.setReversalOf(transfer());
        when(movementRepository.findById(reversal.getId())).thenReturn(Optional.of(reversal));
        assertThrows(ConflictException.class, () -> service.reverse(reversal.getId(), new StockReversalRequest()));
    }

    @Test
    void workOrderMovementsAreNotReversedHere() {
        StockMovement original = transfer();
        original.setWorkOrder(WorkOrder.builder().id(UUID.randomUUID()).build());
        when(movementRepository.findById(original.getId())).thenReturn(Optional.of(original));
        assertThrows(ConflictException.class, () -> service.reverse(original.getId(), new StockReversalRequest()));
        verify(movementRepository, never()).save(any());
    }
}
