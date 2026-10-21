package com.github.rrin.inventory.repository;

import com.github.rrin.inventory.LocationKind;
import com.github.rrin.inventory.QualityState;
import com.github.rrin.inventory.StockBalance;
import com.github.rrin.inventory.StockBalanceId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockBalanceRepository extends JpaRepository<StockBalance, StockBalanceId> {

    // locked so two movements touching the same bucket can't both pass the non-negative check
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM StockBalance b WHERE b.id = :id")
    Optional<StockBalance> findForUpdate(StockBalanceId id);

    // empty buckets are kept as rows but hidden from listings
    @Query("SELECT b FROM StockBalance b WHERE (:itemId IS NULL OR b.id.itemId = :itemId) "
            + "AND (:locationId IS NULL OR b.id.locationId = :locationId) AND b.qty <> 0 "
            + "ORDER BY b.id.itemId, b.id.locationId, b.id.quality")
    List<StockBalance> findNonZero(UUID itemId, UUID locationId);

    @Query("SELECT COALESCE(SUM(b.qty), 0) FROM StockBalance b, Location l "
            + "WHERE l.id = b.id.locationId AND b.id.itemId = :itemId AND b.id.quality = :quality AND l.kind = :kind")
    BigDecimal sumByKind(UUID itemId, QualityState quality, LocationKind kind);
}
