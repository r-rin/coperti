package com.github.rrin.inventory.repository;

import com.github.rrin.inventory.MovementType;
import com.github.rrin.inventory.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Append-only: callers only ever save new rows. Corrections are compensating rows, never updates or deletes. */
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID>, JpaSpecificationExecutor<StockMovement> {

    @Query("SELECT COUNT(m) > 0 FROM StockMovement m WHERE m.fromLocation.id = :locationId OR m.toLocation.id = :locationId")
    boolean existsTouchingLocation(UUID locationId);

    boolean existsByReversalOf_Id(UUID movementId);

    List<StockMovement> findAllByOrderByOccurredAtAscIdAsc();

    @Query("SELECT COALESCE(SUM(m.qty), 0) FROM StockMovement m WHERE m.step.id = :stepId AND m.movementType = :type")
    BigDecimal sumForStep(UUID stepId, MovementType type);
}
