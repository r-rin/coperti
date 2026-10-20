package com.github.rrin.production.service.implementation;

import com.github.rrin.exception.PageConstraintValidator;
import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.InvalidQuery;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.identity.Employee;
import com.github.rrin.identity.repository.EmployeeRepository;
import com.github.rrin.item.Item;
import com.github.rrin.item.ItemCategory;
import com.github.rrin.item.repository.ItemRepository;
import com.github.rrin.process.Process;
import com.github.rrin.process.ProcessComponent;
import com.github.rrin.process.ProcessStatus;
import com.github.rrin.process.ProcessStep;
import com.github.rrin.process.repository.ProcessComponentRepository;
import com.github.rrin.process.repository.ProcessRepository;
import com.github.rrin.production.*;
import com.github.rrin.production.dto.WorkOrderRequest;
import com.github.rrin.production.dto.filter.WorkOrderFilter;
import com.github.rrin.production.repository.WorkOrderComponentRepository;
import com.github.rrin.production.repository.WorkOrderRepository;
import com.github.rrin.production.repository.WorkOrderStepRepository;
import com.github.rrin.production.repository.specs.WorkOrderSpecs;
import com.github.rrin.production.service.ProductionOrderService;
import com.github.rrin.production.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderStepRepository stepRepository;
    private final WorkOrderComponentRepository componentRepository;
    private final ProcessRepository processRepository;
    private final ProcessComponentRepository processComponentRepository;
    private final ItemRepository itemRepository;
    private final EmployeeRepository employeeRepository;
    private final ProductionOrderService productionOrderService;
    private final StepStatusRecorder statusRecorder;

    @Autowired
    public WorkOrderServiceImpl(WorkOrderRepository workOrderRepository,
                                WorkOrderStepRepository stepRepository,
                                WorkOrderComponentRepository componentRepository,
                                ProcessRepository processRepository,
                                ProcessComponentRepository processComponentRepository,
                                ItemRepository itemRepository,
                                EmployeeRepository employeeRepository,
                                ProductionOrderService productionOrderService,
                                StepStatusRecorder statusRecorder) {
        this.workOrderRepository = workOrderRepository;
        this.stepRepository = stepRepository;
        this.componentRepository = componentRepository;
        this.processRepository = processRepository;
        this.processComponentRepository = processComponentRepository;
        this.itemRepository = itemRepository;
        this.employeeRepository = employeeRepository;
        this.productionOrderService = productionOrderService;
        this.statusRecorder = statusRecorder;
    }

    @Override
    public WorkOrder get(UUID id) {
        return getIfExists(id);
    }

    @Override
    public Page<WorkOrder> search(WorkOrderFilter filter, int page, int size) {
        new PageConstraintValidator(page, size).throwIfAny(InvalidQuery::new);
        return workOrderRepository.findAll(WorkOrderSpecs.matching(filter),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "releasedAt")));
    }

    @Override
    @Transactional
    public WorkOrder release(WorkOrderRequest request) {
        new ValidationCheck()
                .check(request.getCode() != null && !request.getCode().isBlank(), "Code is required")
                .check(request.getQuantity() != null && request.getQuantity().compareTo(BigDecimal.ZERO) > 0,
                        "Quantity must be greater than 0")
                .check(request.getProcessId() != null || request.getItemId() != null,
                        "Either process id or item id is required")
                .throwIfAny(ValidationException::new);
        new ValidationCheck()
                .check(!workOrderRepository.existsByCode(request.getCode()),
                        "Work order with code " + request.getCode() + " already exists")
                .throwIfAny(ConflictException::new);

        Process process = resolveProcess(request);
        ProductionOrder order = resolveProductionOrder(request.getProductionOrderId(), process);
        Map<ProcessStep, List<ProcessComponent>> bom = loadBom(process);
        Map<UUID, Item> substitutes = resolveSubstitutes(bom, request.getSubstitutes());

        WorkOrder workOrder = workOrderRepository.save(WorkOrder.builder()
                .code(request.getCode())
                .order(order)
                .item(process.getProduces())
                .process(process)
                .quantity(request.getQuantity())
                .kind(request.getKind() != null ? request.getKind() : WorkOrderKind.PRODUCTION)
                .primaryAssignee(getOptionalEmployee(request.getPrimaryAssigneeId()))
                .status(WorkOrderStatus.RELEASED)
                .releasedAt(Instant.now())
                .build());

        boolean first = true;
        for (Map.Entry<ProcessStep, List<ProcessComponent>> entry : bom.entrySet()) {
            ProcessStep processStep = entry.getKey();
            WorkOrderStep step = stepRepository.save(WorkOrderStep.builder()
                    .workOrder(workOrder)
                    .seq(processStep.getSeq())
                    .operation(processStep.getOperation())
                    .outputItem(processStep.getOutputItem())
                    .outputQuantity(processStep.getOutputItem() != null
                            ? BigDecimal.valueOf(processStep.getOutputQuantity()).multiply(request.getQuantity())
                            : null)
                    // only the first step can be picked up right away; the rest wait for their predecessor
                    .status(first ? WorkOrderStepStatus.READY : WorkOrderStepStatus.PLANNED)
                    .build());
            first = false;

            for (ProcessComponent line : entry.getValue()) {
                Item item = line.getConsumed() != null ? line.getConsumed() : substitutes.get(line.getId());
                componentRepository.save(WorkOrderComponent.builder()
                        .workOrder(workOrder)
                        .step(step)
                        .item(item)
                        // BOM quantities are per one produced unit
                        .qtyRequired(BigDecimal.valueOf(line.getConsumedQuantity()).multiply(request.getQuantity()))
                        .build());
            }
        }
        return workOrder;
    }

    @Override
    public WorkOrder complete(UUID id) {
        WorkOrder workOrder = getIfExists(id);
        WorkOrderGuard.requireOpen(workOrder, "complete work order");

        List<WorkOrderStep> steps = stepRepository.findAllByWorkOrder_IdOrderBySeqAsc(id);
        new ValidationCheck()
                .check(steps.stream().allMatch(step -> step.getStatus().isTerminal()),
                        "Cannot complete work order " + workOrder.getCode() + ": some steps are not finished")
                .check(steps.stream().anyMatch(step -> step.getStatus() == WorkOrderStepStatus.DONE),
                        "Cannot complete work order " + workOrder.getCode() + ": no step was done, cancel it instead")
                .throwIfAny(ConflictException::new);

        workOrder.setStatus(WorkOrderStatus.COMPLETED);
        workOrder.setCompletedAt(Instant.now());
        return workOrderRepository.save(workOrder);
    }

    @Override
    @Transactional
    public WorkOrder cancel(UUID id, UUID changedById) {
        WorkOrder workOrder = getIfExists(id);
        WorkOrderGuard.requireOpen(workOrder, "cancel work order");
        Employee changedBy = getOptionalEmployee(changedById);

        for (WorkOrderStep step : stepRepository.findAllByWorkOrder_IdOrderBySeqAsc(id)) {
            if (!step.getStatus().isTerminal()) {
                statusRecorder.record(step, WorkOrderStepStatus.CANCELLED, changedBy);
            }
        }
        workOrder.setStatus(WorkOrderStatus.CANCELLED);
        return workOrderRepository.save(workOrder);
    }

    @Override
    public WorkOrder markInProgress(UUID id) {
        WorkOrder workOrder = getIfExists(id);
        if (workOrder.getStatus() != WorkOrderStatus.RELEASED) {
            return workOrder;
        }
        workOrder.setStatus(WorkOrderStatus.IN_PROGRESS);
        WorkOrder saved = workOrderRepository.save(workOrder);
        if (workOrder.getOrder() != null) {
            productionOrderService.markInProgress(workOrder.getOrder().getId());
        }
        return saved;
    }

    private Process resolveProcess(WorkOrderRequest request) {
        Process process;
        if (request.getProcessId() != null) {
            process = processRepository.findById(request.getProcessId()).orElseThrow(
                    () -> new EntityNotFoundException("Process with id " + request.getProcessId() + " not found"));
            new ValidationCheck()
                    .check(request.getItemId() == null || request.getItemId().equals(process.getProduces().getId()),
                            "Process " + process.getId() + " does not produce item " + request.getItemId())
                    .throwIfAny(ValidationException::new);
        } else {
            process = processRepository.findFirstByProduces_IdAndStatus(request.getItemId(), ProcessStatus.ACTIVE)
                    .orElseThrow(() -> new ConflictException("Item " + request.getItemId() + " has no active process"));
        }
        // drafts can still change and archived versions are retired, so only the live recipe is released
        new ValidationCheck()
                .check(process.getStatus() == ProcessStatus.ACTIVE,
                        "Process " + process.getId() + " is " + process.getStatus() + ", only an ACTIVE process can be released")
                .throwIfAny(ConflictException::new);
        return process;
    }

    private ProductionOrder resolveProductionOrder(UUID productionOrderId, Process process) {
        if (productionOrderId == null) return null;
        ProductionOrder order = productionOrderService.get(productionOrderId);
        new ValidationCheck()
                .check(order.getStatus() == ProductionOrderStatus.RELEASED
                                || order.getStatus() == ProductionOrderStatus.IN_PROGRESS,
                        "Production order " + order.getCode() + " is " + order.getStatus()
                                + ", work orders can only be released against a RELEASED or IN_PROGRESS order")
                .throwIfAny(ConflictException::new);
        new ValidationCheck()
                .check(order.getProduct().getId().equals(process.getProduces().getId()),
                        "Production order " + order.getCode() + " is for a different item than the process produces")
                .throwIfAny(ValidationException::new);
        return order;
    }

    private Map<ProcessStep, List<ProcessComponent>> loadBom(Process process) {
        Map<ProcessStep, List<ProcessComponent>> bom = new LinkedHashMap<>();
        process.getSteps().stream()
                .sorted(Comparator.comparingInt(ProcessStep::getSeq))
                .forEach(step -> bom.put(step, processComponentRepository.findAllByProcessStep_Id(step.getId())));
        return bom;
    }

    /** Every category line needs exactly one concrete item from that category (or a sub-category) to build with. */
    private Map<UUID, Item> resolveSubstitutes(Map<ProcessStep, List<ProcessComponent>> bom, Map<UUID, UUID> requested) {
        Map<UUID, UUID> picks = requested != null ? requested : Map.of();
        Map<UUID, ProcessComponent> categoryLines = new HashMap<>();
        bom.values().forEach(lines -> lines.stream()
                .filter(line -> line.getConsumableCategory() != null)
                .forEach(line -> categoryLines.put(line.getId(), line)));

        ValidationCheck shape = new ValidationCheck();
        categoryLines.keySet().forEach(lineId -> shape.check(picks.get(lineId) != null,
                "Substitute item is required for category component " + lineId));
        picks.keySet().forEach(lineId -> shape.check(categoryLines.containsKey(lineId),
                "Component " + lineId + " is not a category line of the process"));
        shape.throwIfAny(ValidationException::new);

        Map<UUID, Item> resolved = new HashMap<>();
        ValidationCheck membership = new ValidationCheck();
        categoryLines.forEach((lineId, line) -> {
            UUID itemId = picks.get(lineId);
            Item item = itemRepository.findById(itemId).orElseThrow(
                    () -> new EntityNotFoundException("Item with id " + itemId + " not found"));
            membership.check(belongsTo(item, line.getConsumableCategory()),
                    "Item " + item.getName() + " is not in category " + line.getConsumableCategory().getName());
            resolved.put(lineId, item);
        });
        membership.throwIfAny(ValidationException::new);
        return resolved;
    }

    private boolean belongsTo(Item item, ItemCategory category) {
        Set<UUID> seen = new HashSet<>();
        for (ItemCategory current = item.getCategory(); current != null && seen.add(current.getId());
             current = current.getParent()) {
            if (current.getId().equals(category.getId())) {
                return true;
            }
        }
        return false;
    }

    private Employee getOptionalEmployee(UUID id) {
        if (id == null) return null;
        return employeeRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Employee with id " + id + " not found"));
    }

    private WorkOrder getIfExists(UUID id) {
        return workOrderRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Work order with id " + id + " not found"));
    }
}
