package com.github.rrin;

import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.identity.Employee;
import com.github.rrin.identity.repository.EmployeeRepository;
import com.github.rrin.inventory.Location;
import com.github.rrin.inventory.LocationKind;
import com.github.rrin.inventory.dto.LocationRequest;
import com.github.rrin.inventory.repository.LocationRepository;
import com.github.rrin.inventory.repository.StockMovementRepository;
import com.github.rrin.inventory.service.implementation.LocationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private StockMovementRepository movementRepository;

    @InjectMocks
    private LocationServiceImpl service;

    @Test
    void createWarehouse() {
        when(locationRepository.existsByCode("W1")).thenReturn(false);
        when(locationRepository.save(any(Location.class))).thenAnswer(i -> i.getArgument(0));

        Location created = service.create(LocationRequest.builder().code("W1").kind(LocationKind.WAREHOUSE).build());

        assertEquals(LocationKind.WAREHOUSE, created.getKind());
    }

    @Test
    void employeeLocationNeedsEmployee() {
        assertThrows(ValidationException.class,
                () -> service.create(LocationRequest.builder().code("E").kind(LocationKind.EMPLOYEE).build()));
    }

    @Test
    void onlyEmployeeLocationsTakeEmployee() {
        assertThrows(ValidationException.class, () -> service.create(LocationRequest.builder()
                .code("W").kind(LocationKind.WAREHOUSE).employeeId(UUID.randomUUID()).build()));
    }

    @Test
    void virtualLocationsAreNotCreatedByHand() {
        assertThrows(ValidationException.class,
                () -> service.create(LocationRequest.builder().code("S").kind(LocationKind.V_SCRAP).build()));
    }

    @Test
    void duplicateCodeRejected() {
        when(locationRepository.existsByCode("W1")).thenReturn(true);
        assertThrows(ConflictException.class,
                () -> service.create(LocationRequest.builder().code("W1").kind(LocationKind.WAREHOUSE).build()));
    }

    @Test
    void getVirtualCreatesOnFirstUse() {
        when(locationRepository.findFirstByKind(LocationKind.V_SCRAP)).thenReturn(Optional.empty());
        when(locationRepository.save(any(Location.class))).thenAnswer(i -> i.getArgument(0));

        Location scrap = service.getVirtual(LocationKind.V_SCRAP);

        assertEquals("V_SCRAP", scrap.getCode());
        assertEquals(LocationKind.V_SCRAP, scrap.getKind());
    }

    @Test
    void getVirtualRejectsRealKind() {
        assertThrows(ValidationException.class, () -> service.getVirtual(LocationKind.WAREHOUSE));
    }

    @Test
    void getForEmployeeReusesExisting() {
        Employee worker = Employee.builder().id(UUID.randomUUID()).name("worker").build();
        Location hands = Location.builder().id(UUID.randomUUID()).kind(LocationKind.EMPLOYEE).employee(worker).build();
        when(employeeRepository.findById(worker.getId())).thenReturn(Optional.of(worker));
        when(locationRepository.findByEmployee_Id(worker.getId())).thenReturn(Optional.of(hands));

        assertEquals(hands, service.getForEmployee(worker.getId()));
        verify(locationRepository, never()).save(any());
    }

    @Test
    void deleteWithMovementsRejected() {
        UUID id = UUID.randomUUID();
        when(locationRepository.findById(id)).thenReturn(Optional.of(
                Location.builder().id(id).code("W1").kind(LocationKind.WAREHOUSE).build()));
        when(movementRepository.existsTouchingLocation(id)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.delete(id));
        verify(locationRepository, never()).delete(any(Location.class));
    }

    @Test
    void kindCannotChangeOnceStockMoved() {
        UUID id = UUID.randomUUID();
        when(locationRepository.findById(id)).thenReturn(Optional.of(
                Location.builder().id(id).code("W1").kind(LocationKind.WAREHOUSE).build()));
        when(locationRepository.existsByCodeAndIdNot("W1", id)).thenReturn(false);
        when(movementRepository.existsTouchingLocation(id)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.update(LocationRequest.builder()
                .id(id).code("W1").kind(LocationKind.REPAIR).build()));
    }
}
