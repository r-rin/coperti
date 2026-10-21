package com.github.rrin;

import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.identity.Employee;
import com.github.rrin.identity.repository.EmployeeRepository;
import com.github.rrin.inventory.*;
import com.github.rrin.inventory.dto.OtherUseDecisionRequest;
import com.github.rrin.inventory.dto.OtherUseLineRequest;
import com.github.rrin.inventory.dto.OtherUseRequest;
import com.github.rrin.inventory.dto.StockMovementRequest;
import com.github.rrin.inventory.repository.OtherUseLineRepository;
import com.github.rrin.inventory.repository.OtherUseRepository;
import com.github.rrin.inventory.service.LocationService;
import com.github.rrin.inventory.service.StockMovementService;
import com.github.rrin.inventory.service.implementation.OtherUseServiceImpl;
import com.github.rrin.item.Item;
import com.github.rrin.item.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtherUseServiceImplTest {

    @Mock
    private OtherUseRepository otherUseRepository;

    @Mock
    private OtherUseLineRepository lineRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private LocationService locationService;

    @Mock
    private StockMovementService movementService;

    @InjectMocks
    private OtherUseServiceImpl service;

    private final Employee requester = Employee.builder().id(UUID.randomUUID()).name("requester").build();
    private final Employee approver = Employee.builder().id(UUID.randomUUID()).name("approver").build();
    private final Item screw = Item.builder().id(UUID.randomUUID()).name("screw").build();
    private final Location warehouse = Location.builder().id(UUID.randomUUID()).kind(LocationKind.WAREHOUSE).build();
    private final Location sink = Location.builder().id(UUID.randomUUID()).kind(LocationKind.V_OTHER_USE).build();

    private OtherUse pending() {
        OtherUse otherUse = OtherUse.builder().id(UUID.randomUUID()).requestedBy(requester).reason("samples")
                .status(OtherUseStatus.PENDING).build();
        when(otherUseRepository.findById(otherUse.getId())).thenReturn(Optional.of(otherUse));
        return otherUse;
    }

    private OtherUseLine line(OtherUse otherUse, String qty) {
        return OtherUseLine.builder().otherUse(otherUse).item(screw).fromLocation(warehouse)
                .quality(QualityState.GOOD).qty(new BigDecimal(qty)).build();
    }

    @Test
    void createIsPendingAndMovesNothing() {
        when(employeeRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(locationService.get(warehouse.getId())).thenReturn(warehouse);
        when(itemRepository.findById(screw.getId())).thenReturn(Optional.of(screw));
        when(otherUseRepository.save(any(OtherUse.class))).thenAnswer(i -> i.getArgument(0));

        OtherUse created = service.create(OtherUseRequest.builder().requestedById(requester.getId())
                .lines(List.of(OtherUseLineRequest.builder().itemId(screw.getId())
                        .fromLocationId(warehouse.getId()).qty(BigDecimal.ONE).build()))
                .build());

        assertEquals(OtherUseStatus.PENDING, created.getStatus());
        ArgumentCaptor<OtherUseLine> saved = ArgumentCaptor.forClass(OtherUseLine.class);
        verify(lineRepository).save(saved.capture());
        assertEquals(QualityState.GOOD, saved.getValue().getQuality());
        verify(movementService, never()).record(any());
    }

    @Test
    void createNeedsLines() {
        assertThrows(ValidationException.class,
                () -> service.create(OtherUseRequest.builder().requestedById(requester.getId()).build()));
    }

    @Test
    void approvePostsOneMovementPerLine() {
        OtherUse otherUse = pending();
        when(employeeRepository.findById(approver.getId())).thenReturn(Optional.of(approver));
        when(locationService.getVirtual(LocationKind.V_OTHER_USE)).thenReturn(sink);
        when(lineRepository.findAllByOtherUse_Id(otherUse.getId())).thenReturn(List.of(line(otherUse, "1"), line(otherUse, "2")));
        when(otherUseRepository.save(any(OtherUse.class))).thenAnswer(i -> i.getArgument(0));

        OtherUse approved = service.approve(otherUse.getId(), new OtherUseDecisionRequest(approver.getId()));

        ArgumentCaptor<StockMovementRequest> posted = ArgumentCaptor.forClass(StockMovementRequest.class);
        verify(movementService, times(2)).record(posted.capture());
        posted.getAllValues().forEach(movement -> {
            assertEquals(MovementType.OTHER_USE, movement.getMovementType());
            assertEquals(sink.getId(), movement.getToLocationId());
            assertEquals(otherUse.getId(), movement.getOtherUseId());
        });
        assertEquals(OtherUseStatus.APPROVED, approved.getStatus());
        assertEquals(approver, approved.getApprovedBy());
        assertNotNull(approved.getOccurredAt());
    }

    @Test
    void onlyPendingCanBeDecided() {
        OtherUse otherUse = pending();
        otherUse.setStatus(OtherUseStatus.REJECTED);
        assertThrows(ConflictException.class,
                () -> service.approve(otherUse.getId(), new OtherUseDecisionRequest(approver.getId())));
        verify(movementService, never()).record(any());
    }

    @Test
    void rejectRecordsDecider() {
        OtherUse otherUse = pending();
        when(employeeRepository.findById(approver.getId())).thenReturn(Optional.of(approver));
        when(otherUseRepository.save(any(OtherUse.class))).thenAnswer(i -> i.getArgument(0));

        OtherUse rejected = service.reject(otherUse.getId(), new OtherUseDecisionRequest(approver.getId()));

        assertEquals(OtherUseStatus.REJECTED, rejected.getStatus());
        assertEquals(approver, rejected.getApprovedBy());
        assertNull(rejected.getOccurredAt());
    }
}
