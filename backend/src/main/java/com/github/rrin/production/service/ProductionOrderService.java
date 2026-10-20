package com.github.rrin.production.service;

import com.github.rrin.production.ProductionOrder;
import com.github.rrin.production.dto.ProductionOrderRequest;
import com.github.rrin.production.dto.filter.ProductionOrderFilter;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ProductionOrderService {
    ProductionOrder get(UUID id);
    Page<ProductionOrder> search(ProductionOrderFilter filter, int page, int size);
    ProductionOrder create(ProductionOrderRequest request);
    ProductionOrder update(ProductionOrderRequest request);
    ProductionOrder delete(UUID id);

    ProductionOrder release(UUID id);
    ProductionOrder complete(UUID id);
    ProductionOrder cancel(UUID id);

    /** Called when one of the order's work orders starts; a RELEASED order becomes IN_PROGRESS, others are left alone. */
    ProductionOrder markInProgress(UUID id);
}
