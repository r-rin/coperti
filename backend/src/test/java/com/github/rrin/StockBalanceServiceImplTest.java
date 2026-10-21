package com.github.rrin;

import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.inventory.*;
import com.github.rrin.inventory.repository.LocationRepository;
import com.github.rrin.inventory.repository.StockBalanceRepository;
import com.github.rrin.inventory.repository.StockMovementRepository;
import com.github.rrin.inventory.repository.StockReservationRepository;
import com.github.rrin.inventory.service.implementation.StockBalanceServiceImpl;
import com.github.rrin.item.Item;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockBalanceServiceImplTest {

    @Mock
    private StockBalanceRepository balanceRepository;

    @Mock
    private StockMovementRepository movementRepository;

    @Mock
    private StockReservationRepository reservationRepository;

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private StockBalanceServiceImpl service;

    private final Item screw = Item.builder().id(UUID.randomUUID()).name("screw").build();
    private final Location supplier = Location.builder().id(UUID.randomUUID()).code("S").kind(LocationKind.V_SUPPLIER).build();
    private final Location warehouse = Location.builder().id(UUID.randomUUID()).code("W").kind(LocationKind.WAREHOUSE).build();
    private final Location bench = Location.builder().id(UUID.randomUUID()).code("B").kind(LocationKind.WIP).build();

    private StockMovement movement(Location from, Location to, String qty) {
        return StockMovement.builder().item(screw).qty(new BigDecimal(qty))
                .fromLocation(from).toLocation(to)
                .fromQuality(QualityState.GOOD).toQuality(QualityState.GOOD).build();
    }

    private StockBalanceId key(Location location) {
        return new StockBalanceId(screw.getId(), location.getId(), QualityState.GOOD);
    }

    @Test
    void applyMovesStockBetweenBothSides() {
        when(balanceRepository.findForUpdate(key(warehouse)))
                .thenReturn(Optional.of(StockBalance.builder().id(key(warehouse)).qty(BigDecimal.TEN).build()));
        when(balanceRepository.findForUpdate(key(bench))).thenReturn(Optional.empty());

        service.apply(movement(warehouse, bench, "3"));

        ArgumentCaptor<StockBalance> saved = ArgumentCaptor.forClass(StockBalance.class);
        verify(balanceRepository, times(2)).save(saved.capture());
        assertEquals(0, new BigDecimal("7").compareTo(saved.getAllValues().get(0).getQty()));
        assertEquals(0, new BigDecimal("3").compareTo(saved.getAllValues().get(1).getQty()));
    }

    @Test
    void realLocationCannotGoNegative() {
        when(balanceRepository.findForUpdate(key(warehouse)))
                .thenReturn(Optional.of(StockBalance.builder().id(key(warehouse)).qty(new BigDecimal("2")).build()));

        assertThrows(ConflictException.class, () -> service.apply(movement(warehouse, bench, "3")));
        verify(balanceRepository, never()).save(any());
    }

    @Test
    void virtualLocationIsUnbounded() {
        when(balanceRepository.findForUpdate(any())).thenReturn(Optional.empty());

        service.apply(movement(supplier, warehouse, "5"));

        ArgumentCaptor<StockBalance> saved = ArgumentCaptor.forClass(StockBalance.class);
        verify(balanceRepository, times(2)).save(saved.capture());
        assertEquals(0, new BigDecimal("-5").compareTo(saved.getAllValues().get(0).getQty()));
    }

    @Test
    void availableSubtractsOpenReservations() {
        when(balanceRepository.sumByKind(screw.getId(), QualityState.GOOD, LocationKind.WAREHOUSE)).thenReturn(BigDecimal.TEN);
        when(reservationRepository.sumHeldForItem(eq(screw.getId()), eq(ReservationStatus.RESERVED), anyCollection()))
                .thenReturn(new BigDecimal("4"));

        assertEquals(0, new BigDecimal("6").compareTo(service.available(screw.getId())));
    }

    @Test
    void employeeWithoutLocationHoldsNothing() {
        UUID employeeId = UUID.randomUUID();
        when(locationRepository.findByEmployee_Id(employeeId)).thenReturn(Optional.empty());

        assertEquals(List.of(), service.find(null, null, employeeId));
        verify(balanceRepository, never()).findNonZero(any(), any());
    }

    @Test
    void rebuildReplaysLedgerWithoutRejectingDips() {
        when(movementRepository.findAllByOrderByOccurredAtAscIdAsc()).thenReturn(List.of(movement(warehouse, bench, "3")));
        when(balanceRepository.findForUpdate(any())).thenReturn(Optional.empty());

        service.rebuild();

        verify(balanceRepository).deleteAllInBatch();
        verify(balanceRepository, times(2)).save(any(StockBalance.class));
    }
}
