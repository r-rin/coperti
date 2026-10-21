package com.github.rrin.inventory.repository;

import com.github.rrin.inventory.ReservationStatus;
import com.github.rrin.inventory.StockReservation;
import com.github.rrin.production.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {

    List<StockReservation> findAllByWorkOrder_IdOrderByCreatedAtAsc(UUID workOrderId);

    List<StockReservation> findAllByWorkOrder_IdAndStatus(UUID workOrderId, ReservationStatus status);

    List<StockReservation> findAllByWorkOrderComponent_IdAndStatusOrderByCreatedAtAsc(UUID componentId, ReservationStatus status);

    // only reservations of open work orders hold stock back; a finished or cancelled order's promises lapse by themselves
    @Query("SELECT COALESCE(SUM(r.qty), 0) FROM StockReservation r WHERE r.item.id = :itemId "
            + "AND r.status = :status AND r.workOrder.status IN :openStatuses")
    BigDecimal sumHeldForItem(UUID itemId, ReservationStatus status, Collection<WorkOrderStatus> openStatuses);

    @Query("SELECT COALESCE(SUM(r.qty), 0) FROM StockReservation r WHERE r.workOrderComponent.id = :componentId "
            + "AND r.status = :status")
    BigDecimal sumForComponent(UUID componentId, ReservationStatus status);
}
