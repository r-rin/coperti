package com.github.rrin;

import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.identity.repository.EmployeeRepository;
import com.github.rrin.item.Item;
import com.github.rrin.item.repository.ItemRepository;
import com.github.rrin.production.ProductionOrder;
import com.github.rrin.production.ProductionOrderStatus;
import com.github.rrin.production.dto.ProductionOrderRequest;
import com.github.rrin.production.repository.ProductionOrderRepository;
import com.github.rrin.production.repository.WorkOrderRepository;
import com.github.rrin.production.service.implementation.ProductionOrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductionOrderServiceImplTest {

    @Mock
    private ProductionOrderRepository orderRepository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ProductionOrderServiceImpl service;

    private ProductionOrder order(UUID id, ProductionOrderStatus status) {
        return ProductionOrder.builder().id(id).code("PO-1").quantity(BigDecimal.TEN).status(status).build();
    }

    private ProductionOrderRequest request(UUID productId) {
        return ProductionOrderRequest.builder().code("PO-1").productId(productId).quantity(BigDecimal.TEN).build();
    }

    @Test
    void createAlwaysStartsAsDraft() {
        UUID productId = UUID.randomUUID();
        when(orderRepository.existsByCode("PO-1")).thenReturn(false);
        when(itemRepository.findById(productId)).thenReturn(Optional.of(Item.builder().id(productId).build()));
        when(orderRepository.save(any(ProductionOrder.class))).thenAnswer(i -> i.getArgument(0));

        ProductionOrder created = service.create(request(productId));

        assertEquals(ProductionOrderStatus.DRAFT, created.getStatus());
        assertEquals(productId, created.getProduct().getId());
    }

    @Test
    void createRejectsMissingFields() {
        assertThrows(ValidationException.class,
                () -> service.create(ProductionOrderRequest.builder().quantity(BigDecimal.ZERO).build()));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createRejectsDuplicateCode() {
        when(orderRepository.existsByCode("PO-1")).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.create(request(UUID.randomUUID())));
    }

    @Test
    void createThrowsWhenProductMissing() {
        UUID productId = UUID.randomUUID();
        when(orderRepository.existsByCode("PO-1")).thenReturn(false);
        when(itemRepository.findById(productId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.create(request(productId)));
    }

    @Test
    void updateRejectedOnceReleased() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.of(order(id, ProductionOrderStatus.RELEASED)));
        ProductionOrderRequest request = request(UUID.randomUUID());
        request.setId(id);
        assertThrows(ConflictException.class, () -> service.update(request));
    }

    @Test
    void deleteOnlyDrafts() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.of(order(id, ProductionOrderStatus.IN_PROGRESS)));
        assertThrows(ConflictException.class, () -> service.delete(id));
        verify(orderRepository, never()).delete(any(ProductionOrder.class));
    }

    @Test
    void releaseDraft() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.of(order(id, ProductionOrderStatus.DRAFT)));
        when(orderRepository.save(any(ProductionOrder.class))).thenAnswer(i -> i.getArgument(0));
        assertEquals(ProductionOrderStatus.RELEASED, service.release(id).getStatus());
    }

    @Test
    void completeRejectedWhileWorkOrdersOpen() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.of(order(id, ProductionOrderStatus.IN_PROGRESS)));
        when(workOrderRepository.existsByOrder_IdAndStatusIn(eq(id), anyCollection())).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.complete(id));
    }

    @Test
    void completeWhenNoWorkOrdersOpen() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.of(order(id, ProductionOrderStatus.IN_PROGRESS)));
        when(workOrderRepository.existsByOrder_IdAndStatusIn(eq(id), anyCollection())).thenReturn(false);
        when(orderRepository.save(any(ProductionOrder.class))).thenAnswer(i -> i.getArgument(0));
        assertEquals(ProductionOrderStatus.COMPLETED, service.complete(id).getStatus());
    }

    @Test
    void cancelRejectedWhenCompleted() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.of(order(id, ProductionOrderStatus.COMPLETED)));
        assertThrows(ConflictException.class, () -> service.cancel(id));
    }

    @Test
    void markInProgressOnlyMovesReleased() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.of(order(id, ProductionOrderStatus.RELEASED)));
        when(orderRepository.save(any(ProductionOrder.class))).thenAnswer(i -> i.getArgument(0));
        assertEquals(ProductionOrderStatus.IN_PROGRESS, service.markInProgress(id).getStatus());

        UUID other = UUID.randomUUID();
        when(orderRepository.findById(other)).thenReturn(Optional.of(order(other, ProductionOrderStatus.IN_PROGRESS)));
        assertEquals(ProductionOrderStatus.IN_PROGRESS, service.markInProgress(other).getStatus());
    }
}
