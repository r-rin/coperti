package com.github.rrin.inventory.service;

import com.github.rrin.inventory.Location;
import com.github.rrin.inventory.LocationKind;
import com.github.rrin.inventory.dto.LocationRequest;

import java.util.List;
import java.util.UUID;

public interface LocationService {
    Location get(UUID id);

    /** Null kind lists every location. */
    List<Location> getAll(LocationKind kind);

    Location create(LocationRequest request);
    Location update(LocationRequest request);
    Location delete(UUID id);

    /** The employee's own location ("in worker Y's hands"), created on first use. */
    Location getForEmployee(UUID employeeId);

    /** The single location of a virtual kind (supplier, consumed, scrap, other-use), created on first use. */
    Location getVirtual(LocationKind kind);
}
