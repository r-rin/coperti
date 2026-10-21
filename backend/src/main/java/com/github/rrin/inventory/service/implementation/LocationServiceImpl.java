package com.github.rrin.inventory.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.identity.Employee;
import com.github.rrin.identity.repository.EmployeeRepository;
import com.github.rrin.inventory.Location;
import com.github.rrin.inventory.LocationKind;
import com.github.rrin.inventory.dto.LocationRequest;
import com.github.rrin.inventory.repository.LocationRepository;
import com.github.rrin.inventory.repository.StockMovementRepository;
import com.github.rrin.inventory.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final EmployeeRepository employeeRepository;
    private final StockMovementRepository movementRepository;

    @Autowired
    public LocationServiceImpl(LocationRepository locationRepository,
                               EmployeeRepository employeeRepository,
                               StockMovementRepository movementRepository) {
        this.locationRepository = locationRepository;
        this.employeeRepository = employeeRepository;
        this.movementRepository = movementRepository;
    }

    @Override
    public Location get(UUID id) {
        return locationRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Location with id " + id + " not found"));
    }

    @Override
    public List<Location> getAll(LocationKind kind) {
        return kind == null
                ? locationRepository.findAllByOrderByCodeAsc()
                : locationRepository.findAllByKindOrderByCodeAsc(kind);
    }

    @Override
    public Location create(LocationRequest request) {
        validateFields(request);
        Employee employee = getOptionalEmployee(request.getEmployeeId());
        new ValidationCheck()
                .check(!locationRepository.existsByCode(request.getCode()),
                        "Location with code " + request.getCode() + " already exists")
                .check(employee == null || !locationRepository.existsByEmployee_Id(employee.getId()),
                        "Employee " + request.getEmployeeId() + " already has a location")
                .throwIfAny(ConflictException::new);

        return locationRepository.save(Location.builder()
                .code(request.getCode())
                .name(request.getName())
                .kind(request.getKind())
                .employee(employee)
                .build());
    }

    @Override
    public Location update(LocationRequest request) {
        Location location = get(request.getId());
        validateFields(request);
        Employee employee = getOptionalEmployee(request.getEmployeeId());

        boolean identityChanged = location.getKind() != request.getKind()
                || !Objects.equals(location.getEmployee() != null ? location.getEmployee().getId() : null,
                request.getEmployeeId());
        new ValidationCheck()
                .check(!locationRepository.existsByCodeAndIdNot(request.getCode(), location.getId()),
                        "Location with code " + request.getCode() + " already exists")
                // the ledger says stock went "to worker Y"; repointing the place would rewrite that history
                .check(!identityChanged || !movementRepository.existsTouchingLocation(location.getId()),
                        "Kind and employee of location " + location.getCode() + " cannot change once stock has moved through it")
                .check(employee == null || !locationRepository.existsByEmployee_IdAndIdNot(employee.getId(), location.getId()),
                        "Employee " + request.getEmployeeId() + " already has a location")
                .throwIfAny(ConflictException::new);

        location.setCode(request.getCode());
        location.setName(request.getName());
        location.setKind(request.getKind());
        location.setEmployee(employee);
        return locationRepository.save(location);
    }

    @Override
    public Location delete(UUID id) {
        Location location = get(id);
        new ValidationCheck()
                .check(!movementRepository.existsTouchingLocation(id),
                        "Location " + location.getCode() + " has stock movements and cannot be deleted")
                .throwIfAny(ConflictException::new);
        locationRepository.delete(location);
        return location;
    }

    @Override
    public Location getForEmployee(UUID employeeId) {
        Employee employee = getOptionalEmployee(employeeId);
        new ValidationCheck()
                .check(employee != null, "Employee id is required")
                .throwIfAny(ValidationException::new);
        return locationRepository.findByEmployee_Id(employeeId).orElseGet(() -> locationRepository.save(Location.builder()
                .code("EMP-" + employeeId)
                .name(employee.getName())
                .kind(LocationKind.EMPLOYEE)
                .employee(employee)
                .build()));
    }

    @Override
    public Location getVirtual(LocationKind kind) {
        new ValidationCheck()
                .check(kind != null && kind.isVirtual(), "Location kind " + kind + " is not virtual")
                .throwIfAny(ValidationException::new);
        return locationRepository.findFirstByKind(kind).orElseGet(() -> locationRepository.save(Location.builder()
                .code(kind.name())
                .name(kind.name())
                .kind(kind)
                .build()));
    }

    private void validateFields(LocationRequest request) {
        new ValidationCheck()
                .check(request.getCode() != null && !request.getCode().isBlank(), "Code is required")
                .check(request.getKind() != null, "Kind is required")
                // one location per virtual kind keeps sinks unambiguous; getVirtual creates them on demand
                .check(request.getKind() == null || !request.getKind().isVirtual(),
                        "Virtual locations are created automatically")
                .check((request.getKind() == LocationKind.EMPLOYEE) == (request.getEmployeeId() != null),
                        "Employee id must be set exactly for EMPLOYEE locations")
                .throwIfAny(ValidationException::new);
    }

    private Employee getOptionalEmployee(UUID id) {
        if (id == null) return null;
        return employeeRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Employee with id " + id + " not found"));
    }
}
