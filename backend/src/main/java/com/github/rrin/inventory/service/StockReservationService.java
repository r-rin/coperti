package com.github.rrin.inventory.service;

import com.github.rrin.inventory.StockReservation;
import com.github.rrin.inventory.dto.StockReservationRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface StockReservationService {

    /** Strict: fails if the quantity exceeds what's available or what the line still needs. */
    StockReservation reserve(StockReservationRequest request);

    /** Best effort: reserves whatever is available towards every line's outstanding need. */
    List<StockReservation> reserveAll(UUID workOrderId);

    StockReservation release(UUID id);
    List<StockReservation> releaseAll(UUID workOrderId);
    List<StockReservation> getForWorkOrder(UUID workOrderId);
    List<ShortageLine> shortage(UUID workOrderId);

    /** Issuing a line uses up its reservations, oldest first. */
    void drawDown(UUID workOrderComponentId, BigDecimal qty);
}
