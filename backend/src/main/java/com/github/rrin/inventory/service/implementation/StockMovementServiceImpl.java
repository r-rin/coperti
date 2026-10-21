package com.github.rrin.inventory.service.implementation;

import com.github.rrin.exception.PageConstraintValidator;
import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.InvalidQuery;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.identity.Employee;
import com.github.rrin.identity.repository.EmployeeRepository;
import com.github.rrin.inventory.*;
import com.github.rrin.inventory.dto.StockMovementRequest;
import com.github.rrin.inventory.dto.StockReversalRequest;
import com.github.rrin.inventory.dto.filter.StockMovementFilter;
import com.github.rrin.inventory.repository.LocationRepository;
import com.github.rrin.inventory.repository.OtherUseRepository;
import com.github.rrin.inventory.repository.StockMovementRepository;
import com.github.rrin.inventory.repository.specs.StockMovementSpecs;
import com.github.rrin.inventory.service.StockBalanceService;
import com.github.rrin.inventory.service.StockMovementService;
import com.github.rrin.item.Item;
import com.github.rrin.item.repository.ItemRepository;
import com.github.rrin.production.WorkOrder;
import com.github.rrin.production.WorkOrderStep;
import com.github.rrin.production.repository.WorkOrderRepository;
import com.github.rrin.production.repository.WorkOrderStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository movementRepository;
    private final LocationRepository locationRepository;
    private final ItemRepository itemRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderStepRepository stepRepository;
    private final OtherUseRepository otherUseRepository;
    private final StockBalanceService balanceService;

    @Autowired
    public StockMovementServiceImpl(StockMovementRepository movementRepository,
                                    LocationRepository locationRepository,
                                    ItemRepository itemRepository,
                                    EmployeeRepository employeeRepository,
                                    WorkOrderRepository workOrderRepository,
                                    WorkOrderStepRepository stepRepository,
                                    OtherUseRepository otherUseRepository,
                                    StockBalanceService balanceService) {
        this.movementRepository = movementRepository;
        this.locationRepository = locationRepository;
        this.itemRepository = itemRepository;
        this.employeeRepository = employeeRepository;
        this.workOrderRepository = workOrderRepository;
        this.stepRepository = stepRepository;
        this.otherUseRepository = otherUseRepository;
        this.balanceService = balanceService;
    }

    @Override
    public StockMovement get(UUID id) {
        return movementRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Stock movement with id " + id + " not found"));
    }

    @Override
    public Page<StockMovement> search(StockMovementFilter filter, int page, int size) {
        new PageConstraintValidator(page, size).throwIfAny(InvalidQuery::new);
        new ValidationCheck()
                .check(filter.getFrom() == null || filter.getTo() == null || !filter.getTo().isBefore(filter.getFrom()),
                        "End of period cannot be before its start")
                .throwIfAny(InvalidQuery::new);
        return movementRepository.findAll(StockMovementSpecs.matching(filter),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt")));
    }

    @Override
    // record() is a self-call and bypasses the proxy, so the transaction must start here
    @Transactional
    public StockMovement post(StockMovementRequest request) {
        new ValidationCheck()
                .check(request.getMovementType() == null || MovementRules.MANUAL.contains(request.getMovementType()),
                        request.getMovementType() + " movements are posted by work order material, other use or quality actions")
                .check(request.getWorkOrderId() == null && request.getStepId() == null && request.getOtherUseId() == null,
                        "Work order, step and other use links are set by the services that own them")
                .throwIfAny(ValidationException::new);
        return record(request);
    }

    @Override
    @Transactional
    public StockMovement record(StockMovementRequest request) {
        new ValidationCheck()
                .check(request.getItemId() != null, "Item id is required")
                .check(request.getQty() != null && request.getQty().compareTo(BigDecimal.ZERO) > 0,
                        "Quantity must be greater than 0")
                .check(request.getFromLocationId() != null, "From location id is required")
                .check(request.getToLocationId() != null, "To location id is required")
                .check(request.getMovementType() != null, "Movement type is required")
                .throwIfAny(ValidationException::new);

        Location from = getLocation(request.getFromLocationId());
        Location to = getLocation(request.getToLocationId());
        QualityState fromQuality = request.getFromQuality() != null ? request.getFromQuality() : QualityState.GOOD;
        QualityState toQuality = request.getToQuality() != null ? request.getToQuality() : QualityState.GOOD;
        MovementRules.requireShape(request.getMovementType(), from, to, fromQuality, toQuality);

        return save(StockMovement.builder()
                .item(getItem(request.getItemId()))
                .qty(request.getQty())
                .fromLocation(from)
                .toLocation(to)
                .fromQuality(fromQuality)
                .toQuality(toQuality)
                .movementType(request.getMovementType())
                .occurredAt(request.getOccurredAt() != null ? request.getOccurredAt() : Instant.now())
                .performedBy(getOptionalEmployee(request.getPerformedById()))
                .workOrder(getOptionalWorkOrder(request.getWorkOrderId()))
                .step(getOptionalStep(request.getStepId()))
                .otherUse(getOptionalOtherUse(request.getOtherUseId()))
                .note(request.getNote())
                .build());
    }

    @Override
    @Transactional
    public StockMovement reverse(UUID id, StockReversalRequest request) {
        StockMovement original = get(id);
        new ValidationCheck()
                .check(original.getReversalOf() == null, "Movement " + id + " is itself a reversal")
                .check(!movementRepository.existsByReversalOf_Id(id), "Movement " + id + " has already been reversed")
                // their counters live outside the ledger; the owning actions (return, etc.) keep both in step
                .check(original.getWorkOrder() == null && original.getOtherUse() == null,
                        "Movement " + id + " belongs to a work order or other use and is corrected through those")
                .throwIfAny(ConflictException::new);

        // the mirror of a valid row needs no shape check; it may look "backwards" for its type by design
        return save(StockMovement.builder()
                .item(original.getItem())
                .qty(original.getQty())
                .fromLocation(original.getToLocation())
                .toLocation(original.getFromLocation())
                .fromQuality(original.getToQuality())
                .toQuality(original.getFromQuality())
                .movementType(original.getMovementType())
                .occurredAt(Instant.now())
                .performedBy(getOptionalEmployee(request.getPerformedById()))
                .reversalOf(original)
                .note(request.getNote())
                .build());
    }

    private StockMovement save(StockMovement movement) {
        StockMovement saved = movementRepository.save(movement);
        balanceService.apply(saved);
        return saved;
    }

    private Location getLocation(UUID id) {
        return locationRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Location with id " + id + " not found"));
    }

    private Item getItem(UUID id) {
        return itemRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Item with id " + id + " not found"));
    }

    private Employee getOptionalEmployee(UUID id) {
        if (id == null) return null;
        return employeeRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Employee with id " + id + " not found"));
    }

    private WorkOrder getOptionalWorkOrder(UUID id) {
        if (id == null) return null;
        return workOrderRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Work order with id " + id + " not found"));
    }

    private WorkOrderStep getOptionalStep(UUID id) {
        if (id == null) return null;
        return stepRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Work order step with id " + id + " not found"));
    }

    private OtherUse getOptionalOtherUse(UUID id) {
        if (id == null) return null;
        return otherUseRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Other use with id " + id + " not found"));
    }
}
