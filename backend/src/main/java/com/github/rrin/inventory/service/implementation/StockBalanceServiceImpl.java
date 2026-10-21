package com.github.rrin.inventory.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.inventory.*;
import com.github.rrin.inventory.repository.LocationRepository;
import com.github.rrin.inventory.repository.StockBalanceRepository;
import com.github.rrin.inventory.repository.StockMovementRepository;
import com.github.rrin.inventory.repository.StockReservationRepository;
import com.github.rrin.inventory.service.StockBalanceService;
import com.github.rrin.production.WorkOrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
public class StockBalanceServiceImpl implements StockBalanceService {

    private static final EnumSet<WorkOrderStatus> OPEN_WORK_ORDER = EnumSet.of(WorkOrderStatus.RELEASED, WorkOrderStatus.IN_PROGRESS);

    private final StockBalanceRepository balanceRepository;
    private final StockMovementRepository movementRepository;
    private final StockReservationRepository reservationRepository;
    private final LocationRepository locationRepository;

    @Autowired
    public StockBalanceServiceImpl(StockBalanceRepository balanceRepository,
                                   StockMovementRepository movementRepository,
                                   StockReservationRepository reservationRepository,
                                   LocationRepository locationRepository) {
        this.balanceRepository = balanceRepository;
        this.movementRepository = movementRepository;
        this.reservationRepository = reservationRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    @Transactional
    public void apply(StockMovement movement) {
        adjust(movement, movement.getFromLocation(), movement.getFromQuality(), movement.getQty().negate(), true);
        adjust(movement, movement.getToLocation(), movement.getToQuality(), movement.getQty(), true);
    }

    @Override
    public List<StockBalance> find(UUID itemId, UUID locationId, UUID employeeId) {
        if (employeeId != null) {
            UUID employeeLocation = locationRepository.findByEmployee_Id(employeeId).map(Location::getId).orElse(null);
            // an employee without a location has never held anything
            if (employeeLocation == null || locationId != null && !locationId.equals(employeeLocation)) {
                return List.of();
            }
            locationId = employeeLocation;
        }
        return balanceRepository.findNonZero(itemId, locationId);
    }

    @Override
    public BigDecimal available(UUID itemId) {
        BigDecimal onHand = balanceRepository.sumByKind(itemId, QualityState.GOOD, LocationKind.WAREHOUSE);
        BigDecimal held = reservationRepository.sumHeldForItem(itemId, ReservationStatus.RESERVED, OPEN_WORK_ORDER);
        return onHand.subtract(held);
    }

    @Override
    @Transactional
    public void rebuild() {
        balanceRepository.deleteAllInBatch();
        // history may hold backdated rows, so intermediate dips below zero are not errors during a replay
        for (StockMovement movement : movementRepository.findAllByOrderByOccurredAtAscIdAsc()) {
            adjust(movement, movement.getFromLocation(), movement.getFromQuality(), movement.getQty().negate(), false);
            adjust(movement, movement.getToLocation(), movement.getToQuality(), movement.getQty(), false);
        }
    }

    private void adjust(StockMovement movement, Location location, QualityState quality, BigDecimal delta, boolean enforce) {
        StockBalanceId id = new StockBalanceId(movement.getItem().getId(), location.getId(), quality);
        StockBalance balance = balanceRepository.findForUpdate(id)
                .orElseGet(() -> StockBalance.builder().id(id).qty(BigDecimal.ZERO).build());
        BigDecimal next = balance.getQty().add(delta);

        // virtual sinks and sources are unbounded: the supplier "goes negative" by everything it ever delivered
        if (enforce && !location.getKind().isVirtual()) {
            new ValidationCheck()
                    .check(next.compareTo(BigDecimal.ZERO) >= 0,
                            "Not enough " + movement.getItem().getName() + " (" + quality + ") at " + location.getCode()
                                    + ": " + balance.getQty().stripTrailingZeros().toPlainString() + " on hand, "
                                    + delta.negate().stripTrailingZeros().toPlainString() + " requested")
                    .throwIfAny(ConflictException::new);
        }
        balance.setQty(next);
        balanceRepository.save(balance);
    }
}
