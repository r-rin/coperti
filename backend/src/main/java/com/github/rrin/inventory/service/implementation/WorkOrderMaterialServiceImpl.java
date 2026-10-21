package com.github.rrin.inventory.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.inventory.Location;
import com.github.rrin.inventory.LocationKind;
import com.github.rrin.inventory.MovementType;
import com.github.rrin.inventory.StockMovement;
import com.github.rrin.inventory.dto.MaterialMovementRequest;
import com.github.rrin.inventory.dto.StockMovementRequest;
import com.github.rrin.inventory.repository.StockMovementRepository;
import com.github.rrin.inventory.service.LocationService;
import com.github.rrin.inventory.service.StockMovementService;
import com.github.rrin.inventory.service.StockReservationService;
import com.github.rrin.inventory.service.WorkOrderMaterialService;
import com.github.rrin.production.WorkOrderComponent;
import com.github.rrin.production.WorkOrderStep;
import com.github.rrin.production.WorkOrderStepStatus;
import com.github.rrin.production.repository.WorkOrderComponentRepository;
import com.github.rrin.production.repository.WorkOrderStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WorkOrderMaterialServiceImpl implements WorkOrderMaterialService {

    private final WorkOrderComponentRepository componentRepository;
    private final WorkOrderStepRepository stepRepository;
    private final StockMovementRepository movementRepository;
    private final StockMovementService movementService;
    private final StockReservationService reservationService;
    private final LocationService locationService;

    @Autowired
    public WorkOrderMaterialServiceImpl(WorkOrderComponentRepository componentRepository,
                                        WorkOrderStepRepository stepRepository,
                                        StockMovementRepository movementRepository,
                                        StockMovementService movementService,
                                        StockReservationService reservationService,
                                        LocationService locationService) {
        this.componentRepository = componentRepository;
        this.stepRepository = stepRepository;
        this.movementRepository = movementRepository;
        this.movementService = movementService;
        this.reservationService = reservationService;
        this.locationService = locationService;
    }

    @Override
    @Transactional
    public WorkOrderComponent issue(UUID componentId, MaterialMovementRequest request) {
        validateQty(request);
        new ValidationCheck()
                .check(request.getFromLocationId() != null, "From location id is required")
                .throwIfAny(ValidationException::new);
        WorkOrderComponent component = getOpenComponent(componentId, "issue material");

        Location to = request.getToLocationId() != null
                ? locationService.get(request.getToLocationId())
                : assigneeLocation(component);
        // handing parts to a person is an issue; staging them on a WIP bench is a plain transfer
        MovementType type = to.getKind() == LocationKind.EMPLOYEE ? MovementType.ISSUE_TO_EMPLOYEE : MovementType.TRANSFER;
        post(component, type, request.getFromLocationId(), to.getId(), request);

        component.setQtyIssued(component.getQtyIssued().add(request.getQty()));
        reservationService.drawDown(componentId, request.getQty());
        return componentRepository.save(component);
    }

    @Override
    @Transactional
    public WorkOrderComponent consume(UUID componentId, MaterialMovementRequest request) {
        WorkOrderComponent component = useHeld(componentId, request, "consume material");
        post(component, MovementType.CONSUME, request.getFromLocationId(),
                locationService.getVirtual(LocationKind.V_CONSUMED).getId(), request);
        component.setQtyConsumed(component.getQtyConsumed().add(request.getQty()));
        return componentRepository.save(component);
    }

    @Override
    @Transactional
    public WorkOrderComponent scrap(UUID componentId, MaterialMovementRequest request) {
        WorkOrderComponent component = useHeld(componentId, request, "scrap material");
        post(component, MovementType.SCRAP, request.getFromLocationId(),
                locationService.getVirtual(LocationKind.V_SCRAP).getId(), request);
        component.setQtyScrapped(component.getQtyScrapped().add(request.getQty()));
        return componentRepository.save(component);
    }

    @Override
    @Transactional
    public WorkOrderComponent returnUnused(UUID componentId, MaterialMovementRequest request) {
        new ValidationCheck()
                .check(request.getToLocationId() != null, "To location id is required")
                .throwIfAny(ValidationException::new);
        WorkOrderComponent component = useHeld(componentId, request, "return material");

        Location from = locationService.get(request.getFromLocationId());
        MovementType type = from.getKind() == LocationKind.EMPLOYEE ? MovementType.RETURN_FROM_EMPLOYEE : MovementType.TRANSFER;
        post(component, type, from.getId(), request.getToLocationId(), request);
        component.setQtyIssued(component.getQtyIssued().subtract(request.getQty()));
        return componentRepository.save(component);
    }

    @Override
    @Transactional
    public StockMovement produce(UUID stepId, MaterialMovementRequest request) {
        validateQty(request);
        new ValidationCheck()
                .check(request.getToLocationId() != null, "To location id is required")
                .throwIfAny(ValidationException::new);

        WorkOrderStep step = stepRepository.findById(stepId).orElseThrow(
                () -> new EntityNotFoundException("Work order step with id " + stepId + " not found"));
        InventoryGuard.requireOpen(step.getWorkOrder(), "produce output");
        new ValidationCheck()
                .check(step.getOutputItem() != null, "Step " + stepId + " has no output item")
                .check(step.getStatus() == WorkOrderStepStatus.IN_PROGRESS || step.getStatus() == WorkOrderStepStatus.DONE,
                        "Step " + stepId + " is " + step.getStatus() + ", output is produced while IN_PROGRESS or after DONE")
                .throwIfAny(ConflictException::new);
        BigDecimal produced = movementRepository.sumForStep(stepId, MovementType.PRODUCE);
        InventoryGuard.requireWithin(request.getQty(), step.getOutputQuantity().subtract(produced),
                "Step " + stepId + " cannot produce more than its planned output");

        return movementService.record(StockMovementRequest.builder()
                .itemId(step.getOutputItem().getId())
                .qty(request.getQty())
                .fromLocationId(locationService.getVirtual(LocationKind.V_CONSUMED).getId())
                .toLocationId(request.getToLocationId())
                .movementType(MovementType.PRODUCE)
                .performedById(request.getPerformedById())
                .workOrderId(step.getWorkOrder().getId())
                .stepId(stepId)
                .note(request.getNote())
                .build());
    }

    // consume, scrap and return all draw on what was issued and not yet used up
    private WorkOrderComponent useHeld(UUID componentId, MaterialMovementRequest request, String action) {
        validateQty(request);
        new ValidationCheck()
                .check(request.getFromLocationId() != null, "From location id is required")
                .throwIfAny(ValidationException::new);
        WorkOrderComponent component = getOpenComponent(componentId, action);
        BigDecimal held = component.getQtyIssued().subtract(component.getQtyConsumed()).subtract(component.getQtyScrapped());
        InventoryGuard.requireWithin(request.getQty(), held,
                "Cannot " + action + " beyond what was issued to line " + componentId);
        return component;
    }

    private Location assigneeLocation(WorkOrderComponent component) {
        WorkOrderStep step = component.getStep();
        new ValidationCheck()
                .check(step != null && step.getAssignee() != null,
                        "To location id is required when the step has no assignee")
                .throwIfAny(ValidationException::new);
        return locationService.getForEmployee(step.getAssignee().getId());
    }

    private void post(WorkOrderComponent component, MovementType type, UUID fromId, UUID toId, MaterialMovementRequest request) {
        movementService.record(StockMovementRequest.builder()
                .itemId(component.getItem().getId())
                .qty(request.getQty())
                .fromLocationId(fromId)
                .toLocationId(toId)
                .movementType(type)
                .performedById(request.getPerformedById())
                .workOrderId(component.getWorkOrder().getId())
                .stepId(component.getStep() != null ? component.getStep().getId() : null)
                .note(request.getNote())
                .build());
    }

    private void validateQty(MaterialMovementRequest request) {
        new ValidationCheck()
                .check(request.getQty() != null && request.getQty().compareTo(BigDecimal.ZERO) > 0,
                        "Quantity must be greater than 0")
                .throwIfAny(ValidationException::new);
    }

    private WorkOrderComponent getOpenComponent(UUID id, String action) {
        WorkOrderComponent component = componentRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Work order component with id " + id + " not found"));
        InventoryGuard.requireOpen(component.getWorkOrder(), action);
        return component;
    }
}
