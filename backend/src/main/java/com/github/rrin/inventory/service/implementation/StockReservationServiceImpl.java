package com.github.rrin.inventory.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.inventory.ReservationStatus;
import com.github.rrin.inventory.StockReservation;
import com.github.rrin.inventory.dto.StockReservationRequest;
import com.github.rrin.inventory.repository.StockReservationRepository;
import com.github.rrin.inventory.service.ShortageLine;
import com.github.rrin.inventory.service.StockBalanceService;
import com.github.rrin.inventory.service.StockReservationService;
import com.github.rrin.production.WorkOrder;
import com.github.rrin.production.WorkOrderComponent;
import com.github.rrin.production.repository.WorkOrderComponentRepository;
import com.github.rrin.production.repository.WorkOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class StockReservationServiceImpl implements StockReservationService {

    private final StockReservationRepository reservationRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderComponentRepository componentRepository;
    private final StockBalanceService balanceService;

    @Autowired
    public StockReservationServiceImpl(StockReservationRepository reservationRepository,
                                       WorkOrderRepository workOrderRepository,
                                       WorkOrderComponentRepository componentRepository,
                                       StockBalanceService balanceService) {
        this.reservationRepository = reservationRepository;
        this.workOrderRepository = workOrderRepository;
        this.componentRepository = componentRepository;
        this.balanceService = balanceService;
    }

    @Override
    @Transactional
    public StockReservation reserve(StockReservationRequest request) {
        new ValidationCheck()
                .check(request.getWorkOrderComponentId() != null, "Work order component id is required")
                .check(request.getQty() != null && request.getQty().compareTo(BigDecimal.ZERO) > 0,
                        "Quantity must be greater than 0")
                .throwIfAny(ValidationException::new);

        WorkOrderComponent component = getComponent(request.getWorkOrderComponentId());
        InventoryGuard.requireOpen(component.getWorkOrder(), "reserve stock");
        InventoryGuard.requireWithin(request.getQty(), unreservedNeed(component),
                "Reservation exceeds what line " + component.getId() + " still needs");
        InventoryGuard.requireWithin(request.getQty(), balanceService.available(component.getItem().getId()).max(BigDecimal.ZERO),
                "Not enough " + component.getItem().getName() + " available to reserve");

        return save(component, request.getQty());
    }

    @Override
    @Transactional
    public List<StockReservation> reserveAll(UUID workOrderId) {
        WorkOrder workOrder = getWorkOrder(workOrderId);
        InventoryGuard.requireOpen(workOrder, "reserve stock");

        List<StockReservation> created = new ArrayList<>();
        for (WorkOrderComponent component : componentRepository.findAllByWorkOrder_Id(workOrderId)) {
            // availability is re-read per line: earlier lines of the same item already took their share
            BigDecimal qty = unreservedNeed(component)
                    .min(balanceService.available(component.getItem().getId()));
            if (qty.compareTo(BigDecimal.ZERO) > 0) {
                created.add(save(component, qty));
            }
        }
        return created;
    }

    @Override
    public StockReservation release(UUID id) {
        StockReservation reservation = reservationRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Stock reservation with id " + id + " not found"));
        new ValidationCheck()
                .check(reservation.getStatus() == ReservationStatus.RESERVED,
                        "Reservation " + id + " is " + reservation.getStatus() + ", only RESERVED can be released")
                .throwIfAny(ConflictException::new);
        reservation.setStatus(ReservationStatus.RELEASED);
        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public List<StockReservation> releaseAll(UUID workOrderId) {
        getWorkOrder(workOrderId);
        List<StockReservation> active = reservationRepository.findAllByWorkOrder_IdAndStatus(workOrderId, ReservationStatus.RESERVED);
        active.forEach(reservation -> reservation.setStatus(ReservationStatus.RELEASED));
        return reservationRepository.saveAll(active);
    }

    @Override
    public List<StockReservation> getForWorkOrder(UUID workOrderId) {
        getWorkOrder(workOrderId);
        return reservationRepository.findAllByWorkOrder_IdOrderByCreatedAtAsc(workOrderId);
    }

    @Override
    public List<ShortageLine> shortage(UUID workOrderId) {
        getWorkOrder(workOrderId);
        Map<UUID, BigDecimal[]> byItem = new LinkedHashMap<>();
        for (WorkOrderComponent component : componentRepository.findAllByWorkOrder_Id(workOrderId)) {
            BigDecimal[] totals = byItem.computeIfAbsent(component.getItem().getId(),
                    id -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            totals[0] = totals[0].add(component.getQtyRequired().subtract(component.getQtyIssued()).max(BigDecimal.ZERO));
            totals[1] = totals[1].add(reservationRepository.sumForComponent(component.getId(), ReservationStatus.RESERVED));
        }

        List<ShortageLine> lines = new ArrayList<>();
        byItem.forEach((itemId, totals) -> {
            BigDecimal free = balanceService.available(itemId).max(BigDecimal.ZERO);
            BigDecimal missing = totals[0].subtract(totals[1]).subtract(free).max(BigDecimal.ZERO);
            lines.add(new ShortageLine(itemId, totals[0], totals[1], free, missing));
        });
        return lines;
    }

    @Override
    @Transactional
    public void drawDown(UUID workOrderComponentId, BigDecimal qty) {
        BigDecimal remaining = qty;
        for (StockReservation reservation : reservationRepository
                .findAllByWorkOrderComponent_IdAndStatusOrderByCreatedAtAsc(workOrderComponentId, ReservationStatus.RESERVED)) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal taken = remaining.min(reservation.getQty());
            reservation.setQty(reservation.getQty().subtract(taken));
            if (reservation.getQty().compareTo(BigDecimal.ZERO) == 0) {
                reservation.setStatus(ReservationStatus.CONSUMED);
            }
            reservationRepository.save(reservation);
            remaining = remaining.subtract(taken);
        }
    }

    // required − issued − already reserved: what a new reservation for this line may still cover
    private BigDecimal unreservedNeed(WorkOrderComponent component) {
        return component.getQtyRequired()
                .subtract(component.getQtyIssued())
                .subtract(reservationRepository.sumForComponent(component.getId(), ReservationStatus.RESERVED))
                .max(BigDecimal.ZERO);
    }

    private StockReservation save(WorkOrderComponent component, BigDecimal qty) {
        return reservationRepository.save(StockReservation.builder()
                .item(component.getItem())
                .workOrder(component.getWorkOrder())
                .workOrderComponent(component)
                .qty(qty)
                .status(ReservationStatus.RESERVED)
                .build());
    }

    private WorkOrderComponent getComponent(UUID id) {
        return componentRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Work order component with id " + id + " not found"));
    }

    private WorkOrder getWorkOrder(UUID id) {
        return workOrderRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Work order with id " + id + " not found"));
    }
}
