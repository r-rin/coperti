package com.github.rrin.inventory.controller;

import com.github.rrin.inventory.*;
import com.github.rrin.inventory.dto.*;
import com.github.rrin.inventory.service.OtherUseService;
import com.github.rrin.inventory.service.ShortageLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    private final OtherUseService otherUseService;

    @Autowired
    public InventoryMapper(OtherUseService otherUseService) {
        this.otherUseService = otherUseService;
    }

    public LocationResponse toResponse(Location location) {
        return LocationResponse.builder()
                .id(location.getId())
                .code(location.getCode())
                .name(location.getName())
                .kind(location.getKind())
                .employeeId(location.getEmployee() != null ? location.getEmployee().getId() : null)
                .build();
    }

    public StockMovementResponse toResponse(StockMovement movement) {
        return StockMovementResponse.builder()
                .id(movement.getId())
                .itemId(movement.getItem().getId())
                .qty(movement.getQty())
                .fromLocationId(movement.getFromLocation().getId())
                .toLocationId(movement.getToLocation().getId())
                .fromQuality(movement.getFromQuality())
                .toQuality(movement.getToQuality())
                .movementType(movement.getMovementType())
                .occurredAt(movement.getOccurredAt())
                .performedById(movement.getPerformedBy() != null ? movement.getPerformedBy().getId() : null)
                .workOrderId(movement.getWorkOrder() != null ? movement.getWorkOrder().getId() : null)
                .stepId(movement.getStep() != null ? movement.getStep().getId() : null)
                .otherUseId(movement.getOtherUse() != null ? movement.getOtherUse().getId() : null)
                .reversalOfId(movement.getReversalOf() != null ? movement.getReversalOf().getId() : null)
                .note(movement.getNote())
                .build();
    }

    public StockBalanceResponse toResponse(StockBalance balance) {
        return StockBalanceResponse.builder()
                .itemId(balance.getId().getItemId())
                .locationId(balance.getId().getLocationId())
                .quality(balance.getId().getQuality())
                .qty(balance.getQty())
                .build();
    }

    public StockReservationResponse toResponse(StockReservation reservation) {
        return StockReservationResponse.builder()
                .id(reservation.getId())
                .itemId(reservation.getItem().getId())
                .workOrderId(reservation.getWorkOrder().getId())
                .workOrderComponentId(reservation.getWorkOrderComponent().getId())
                .qty(reservation.getQty())
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .build();
    }

    public ShortageResponse toResponse(ShortageLine line) {
        return ShortageResponse.builder()
                .itemId(line.itemId())
                .outstanding(line.outstanding())
                .reserved(line.reserved())
                .freeAvailable(line.freeAvailable())
                .missing(line.missing())
                .build();
    }

    public OtherUseResponse toResponse(OtherUse otherUse) {
        return OtherUseResponse.builder()
                .id(otherUse.getId())
                .requestedById(otherUse.getRequestedBy() != null ? otherUse.getRequestedBy().getId() : null)
                .approvedById(otherUse.getApprovedBy() != null ? otherUse.getApprovedBy().getId() : null)
                .reason(otherUse.getReason())
                .requestedAt(otherUse.getRequestedAt())
                .occurredAt(otherUse.getOccurredAt())
                .status(otherUse.getStatus())
                .lines(otherUseService.getLines(otherUse.getId()).stream().map(this::toResponse).toList())
                .build();
    }

    public OtherUseLineResponse toResponse(OtherUseLine line) {
        return OtherUseLineResponse.builder()
                .id(line.getId())
                .itemId(line.getItem().getId())
                .fromLocationId(line.getFromLocation().getId())
                .quality(line.getQuality())
                .qty(line.getQty())
                .build();
    }
}
