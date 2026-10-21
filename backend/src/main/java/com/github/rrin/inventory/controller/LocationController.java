package com.github.rrin.inventory.controller;

import com.github.rrin.inventory.LocationKind;
import com.github.rrin.inventory.dto.LocationRequest;
import com.github.rrin.inventory.dto.LocationResponse;
import com.github.rrin.inventory.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final LocationService locationService;
    private final InventoryMapper mapper;

    @Autowired
    public LocationController(LocationService locationService, InventoryMapper mapper) {
        this.locationService = locationService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<LocationResponse> getAll(@RequestParam(required = false) LocationKind kind) {
        return locationService.getAll(kind).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public LocationResponse get(@PathVariable UUID id) {
        return mapper.toResponse(locationService.get(id));
    }

    @GetMapping("/employee/{employeeId}")
    public LocationResponse forEmployee(@PathVariable UUID employeeId) {
        return mapper.toResponse(locationService.getForEmployee(employeeId));
    }

    @GetMapping("/virtual/{kind}")
    public LocationResponse virtual(@PathVariable LocationKind kind) {
        return mapper.toResponse(locationService.getVirtual(kind));
    }

    @PostMapping
    public LocationResponse create(@RequestBody LocationRequest request) {
        return mapper.toResponse(locationService.create(request));
    }

    @PutMapping
    public LocationResponse update(@RequestBody LocationRequest request) {
        return mapper.toResponse(locationService.update(request));
    }

    @DeleteMapping("/{id}")
    public LocationResponse delete(@PathVariable UUID id) {
        return mapper.toResponse(locationService.delete(id));
    }
}
