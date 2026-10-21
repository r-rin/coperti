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
import com.github.rrin.inventory.dto.OtherUseDecisionRequest;
import com.github.rrin.inventory.dto.OtherUseLineRequest;
import com.github.rrin.inventory.dto.OtherUseRequest;
import com.github.rrin.inventory.dto.StockMovementRequest;
import com.github.rrin.inventory.dto.filter.OtherUseFilter;
import com.github.rrin.inventory.repository.OtherUseLineRepository;
import com.github.rrin.inventory.repository.OtherUseRepository;
import com.github.rrin.inventory.repository.specs.OtherUseSpecs;
import com.github.rrin.inventory.service.LocationService;
import com.github.rrin.inventory.service.OtherUseService;
import com.github.rrin.inventory.service.StockMovementService;
import com.github.rrin.item.Item;
import com.github.rrin.item.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OtherUseServiceImpl implements OtherUseService {

    private final OtherUseRepository otherUseRepository;
    private final OtherUseLineRepository lineRepository;
    private final EmployeeRepository employeeRepository;
    private final ItemRepository itemRepository;
    private final LocationService locationService;
    private final StockMovementService movementService;

    @Autowired
    public OtherUseServiceImpl(OtherUseRepository otherUseRepository,
                               OtherUseLineRepository lineRepository,
                               EmployeeRepository employeeRepository,
                               ItemRepository itemRepository,
                               LocationService locationService,
                               StockMovementService movementService) {
        this.otherUseRepository = otherUseRepository;
        this.lineRepository = lineRepository;
        this.employeeRepository = employeeRepository;
        this.itemRepository = itemRepository;
        this.locationService = locationService;
        this.movementService = movementService;
    }

    @Override
    public OtherUse get(UUID id) {
        return otherUseRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Other use with id " + id + " not found"));
    }

    @Override
    public List<OtherUseLine> getLines(UUID otherUseId) {
        return lineRepository.findAllByOtherUse_Id(get(otherUseId).getId());
    }

    @Override
    public Page<OtherUse> search(OtherUseFilter filter, int page, int size) {
        new PageConstraintValidator(page, size).throwIfAny(InvalidQuery::new);
        new ValidationCheck()
                .check(filter.getFrom() == null || filter.getTo() == null || !filter.getTo().isBefore(filter.getFrom()),
                        "End of period cannot be before its start")
                .throwIfAny(InvalidQuery::new);
        return otherUseRepository.findAll(OtherUseSpecs.matching(filter),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestedAt")));
    }

    @Override
    @Transactional
    public OtherUse create(OtherUseRequest request) {
        ValidationCheck fields = new ValidationCheck()
                .check(request.getRequestedById() != null, "Requested by id is required")
                .check(request.getLines() != null && !request.getLines().isEmpty(), "At least one line is required");
        if (request.getLines() != null) {
            for (OtherUseLineRequest line : request.getLines()) {
                fields.check(line.getItemId() != null, "Line item id is required")
                        .check(line.getFromLocationId() != null, "Line from location id is required")
                        .check(line.getQty() != null && line.getQty().compareTo(BigDecimal.ZERO) > 0,
                                "Line quantity must be greater than 0");
            }
        }
        fields.throwIfAny(ValidationException::new);

        OtherUse otherUse = otherUseRepository.save(OtherUse.builder()
                .requestedBy(getEmployee(request.getRequestedById()))
                .reason(request.getReason())
                .status(OtherUseStatus.PENDING)
                .build());

        for (OtherUseLineRequest line : request.getLines()) {
            Location from = locationService.get(line.getFromLocationId());
            new ValidationCheck()
                    .check(!from.getKind().isVirtual(), "Stock can only be withdrawn from a real location")
                    .throwIfAny(ValidationException::new);
            lineRepository.save(OtherUseLine.builder()
                    .otherUse(otherUse)
                    .item(getItem(line.getItemId()))
                    .fromLocation(from)
                    .quality(line.getQuality() != null ? line.getQuality() : QualityState.GOOD)
                    .qty(line.getQty())
                    .build());
        }
        return otherUse;
    }

    @Override
    @Transactional
    public OtherUse approve(UUID id, OtherUseDecisionRequest request) {
        OtherUse otherUse = get(id);
        Employee approver = decide(otherUse, request, "approve");

        // all lines move or none do: a shortfall on any line rolls the whole approval back
        UUID sink = locationService.getVirtual(LocationKind.V_OTHER_USE).getId();
        for (OtherUseLine line : lineRepository.findAllByOtherUse_Id(id)) {
            movementService.record(StockMovementRequest.builder()
                    .itemId(line.getItem().getId())
                    .qty(line.getQty())
                    .fromLocationId(line.getFromLocation().getId())
                    .toLocationId(sink)
                    .fromQuality(line.getQuality())
                    .toQuality(line.getQuality())
                    .movementType(MovementType.OTHER_USE)
                    .performedById(approver.getId())
                    .otherUseId(id)
                    .note(otherUse.getReason())
                    .build());
        }

        otherUse.setStatus(OtherUseStatus.APPROVED);
        otherUse.setApprovedBy(approver);
        otherUse.setOccurredAt(Instant.now());
        return otherUseRepository.save(otherUse);
    }

    @Override
    public OtherUse reject(UUID id, OtherUseDecisionRequest request) {
        OtherUse otherUse = get(id);
        otherUse.setApprovedBy(decide(otherUse, request, "reject"));
        otherUse.setStatus(OtherUseStatus.REJECTED);
        return otherUseRepository.save(otherUse);
    }

    private Employee decide(OtherUse otherUse, OtherUseDecisionRequest request, String action) {
        new ValidationCheck()
                .check(request.getEmployeeId() != null, "Employee id is required")
                .throwIfAny(ValidationException::new);
        new ValidationCheck()
                .check(otherUse.getStatus() == OtherUseStatus.PENDING,
                        "Cannot " + action + " other use " + otherUse.getId() + ": it is already " + otherUse.getStatus())
                .throwIfAny(ConflictException::new);
        return getEmployee(request.getEmployeeId());
    }

    private Employee getEmployee(UUID id) {
        return employeeRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Employee with id " + id + " not found"));
    }

    private Item getItem(UUID id) {
        return itemRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Item with id " + id + " not found"));
    }
}
