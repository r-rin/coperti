package com.github.rrin.inventory.repository;

import com.github.rrin.inventory.Location;
import com.github.rrin.inventory.LocationKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, UUID id);
    boolean existsByEmployee_Id(UUID employeeId);
    boolean existsByEmployee_IdAndIdNot(UUID employeeId, UUID id);
    List<Location> findAllByOrderByCodeAsc();
    List<Location> findAllByKindOrderByCodeAsc(LocationKind kind);
    Optional<Location> findFirstByKind(LocationKind kind);
    Optional<Location> findByEmployee_Id(UUID employeeId);
}
