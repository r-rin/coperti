package com.github.rrin.production.service.implementation;

import com.github.rrin.exception.DateRangeConstraintValidator;
import com.github.rrin.exception.PageConstraintValidator;
import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.InvalidQuery;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.identity.Employee;
import com.github.rrin.identity.repository.EmployeeRepository;
import com.github.rrin.item.Item;
import com.github.rrin.item.repository.ItemRepository;
import com.github.rrin.production.ProductionOrder;
import com.github.rrin.production.ProductionOrderStatus;
import com.github.rrin.production.WorkOrderStatus;
import com.github.rrin.production.dto.ProductionOrderRequest;
import com.github.rrin.production.dto.filter.ProductionOrderFilter;
import com.github.rrin.production.repository.ProductionOrderRepository;
import com.github.rrin.production.repository.WorkOrderRepository;
import com.github.rrin.production.repository.specs.ProductionOrderSpecs;
import com.github.rrin.production.service.ProductionOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.UUID;

import static com.github.rrin.production.ProductionOrderStatus.*;

@Service
public class ProductionOrderServiceImpl implements ProductionOrderService {

    private static final EnumSet<WorkOrderStatus> OPEN_WORK_ORDER = EnumSet.of(WorkOrderStatus.RELEASED, WorkOrderStatus.IN_PROGRESS);

    private final ProductionOrderRepository orderRepository;
    private final WorkOrderRepository workOrderRepository;
    private final ItemRepository itemRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public ProductionOrderServiceImpl(ProductionOrderRepository orderRepository,
                                      WorkOrderRepository workOrderRepository,
                                      ItemRepository itemRepository,
                                      EmployeeRepository employeeRepository) {
        this.orderRepository = orderRepository;
        this.workOrderRepository = workOrderRepository;
        this.itemRepository = itemRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public ProductionOrder get(UUID id) {
        return getIfExists(id);
    }

    @Override
    public Page<ProductionOrder> search(ProductionOrderFilter filter, int page, int size) {
        new PageConstraintValidator(page, size).throwIfAny(InvalidQuery::new);
        new DateRangeConstraintValidator(filter.getFromDate(), filter.getToDate()).throwIfAny(InvalidQuery::new);

        return orderRepository.findAll(ProductionOrderSpecs.matching(filter),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @Override
    public ProductionOrder create(ProductionOrderRequest request) {
        validateFields(request);
        new ValidationCheck()
                .check(!orderRepository.existsByCode(request.getCode()),
                        "Production order with code " + request.getCode() + " already exists")
                .throwIfAny(ConflictException::new);

        ProductionOrder order = ProductionOrder.builder()
                .code(request.getCode())
                .product(getItem(request.getProductId()))
                .quantity(request.getQuantity())
                .dueDate(request.getDueDate())
                .description(request.getDescription())
                .responsible(getOptionalEmployee(request.getResponsibleId()))
                .createdBy(getOptionalEmployee(request.getCreatedById()))
                // demand always starts as a draft; it is committed to the floor only through release
                .status(DRAFT)
                .build();
        return orderRepository.save(order);
    }

    @Override
    public ProductionOrder update(ProductionOrderRequest request) {
        ProductionOrder order = getIfExists(request.getId());
        ProductionOrderGuard.requireStatus(order, "update production order", DRAFT);
        validateFields(request);
        new ValidationCheck()
                .check(!orderRepository.existsByCodeAndIdNot(request.getCode(), order.getId()),
                        "Production order with code " + request.getCode() + " already exists")
                .throwIfAny(ConflictException::new);

        order.setCode(request.getCode());
        order.setProduct(getItem(request.getProductId()));
        order.setQuantity(request.getQuantity());
        order.setDueDate(request.getDueDate());
        order.setDescription(request.getDescription());
        order.setResponsible(getOptionalEmployee(request.getResponsibleId()));
        return orderRepository.save(order);
    }

    @Override
    public ProductionOrder delete(UUID id) {
        ProductionOrder order = getIfExists(id);
        // work orders can only be released against a released order, so a draft has none to orphan
        ProductionOrderGuard.requireStatus(order, "delete production order", DRAFT);
        orderRepository.delete(order);
        return order;
    }

    @Override
    public ProductionOrder release(UUID id) {
        ProductionOrder order = getIfExists(id);
        ProductionOrderGuard.requireStatus(order, "release production order", DRAFT);
        return setStatus(order, RELEASED);
    }

    @Override
    public ProductionOrder complete(UUID id) {
        ProductionOrder order = getIfExists(id);
        ProductionOrderGuard.requireStatus(order, "complete production order", RELEASED, IN_PROGRESS);
        ProductionOrderGuard.requireNoOpenWorkOrders(order, "complete production order", hasOpenWorkOrders(order));
        return setStatus(order, COMPLETED);
    }

    @Override
    public ProductionOrder cancel(UUID id) {
        ProductionOrder order = getIfExists(id);
        ProductionOrderGuard.requireStatus(order, "cancel production order", DRAFT, RELEASED, IN_PROGRESS);
        ProductionOrderGuard.requireNoOpenWorkOrders(order, "cancel production order", hasOpenWorkOrders(order));
        return setStatus(order, CANCELLED);
    }

    @Override
    public ProductionOrder markInProgress(UUID id) {
        ProductionOrder order = getIfExists(id);
        return order.getStatus() == RELEASED ? setStatus(order, IN_PROGRESS) : order;
    }

    private boolean hasOpenWorkOrders(ProductionOrder order) {
        return workOrderRepository.existsByOrder_IdAndStatusIn(order.getId(), OPEN_WORK_ORDER);
    }

    private ProductionOrder setStatus(ProductionOrder order, ProductionOrderStatus status) {
        order.setStatus(status);
        return orderRepository.save(order);
    }

    private void validateFields(ProductionOrderRequest request) {
        new ValidationCheck()
                .check(request.getCode() != null && !request.getCode().isBlank(), "Code is required")
                .check(request.getProductId() != null, "Product id is required")
                .check(request.getQuantity() != null && request.getQuantity().compareTo(BigDecimal.ZERO) > 0,
                        "Quantity must be greater than 0")
                .throwIfAny(ValidationException::new);
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

    private ProductionOrder getIfExists(UUID id) {
        return orderRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Production order with id " + id + " not found"));
    }
}
