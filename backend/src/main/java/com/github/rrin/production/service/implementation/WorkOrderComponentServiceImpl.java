package com.github.rrin.production.service.implementation;

import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.production.WorkOrderComponent;
import com.github.rrin.production.repository.WorkOrderComponentRepository;
import com.github.rrin.production.repository.WorkOrderRepository;
import com.github.rrin.production.repository.WorkOrderStepRepository;
import com.github.rrin.production.service.WorkOrderComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WorkOrderComponentServiceImpl implements WorkOrderComponentService {

    private final WorkOrderComponentRepository componentRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderStepRepository stepRepository;

    @Autowired
    public WorkOrderComponentServiceImpl(WorkOrderComponentRepository componentRepository,
                                         WorkOrderRepository workOrderRepository,
                                         WorkOrderStepRepository stepRepository) {
        this.componentRepository = componentRepository;
        this.workOrderRepository = workOrderRepository;
        this.stepRepository = stepRepository;
    }

    @Override
    public WorkOrderComponent get(UUID id) {
        return componentRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Work order component with id " + id + " not found"));
    }

    @Override
    public List<WorkOrderComponent> getAllForWorkOrder(UUID workOrderId) {
        if (!workOrderRepository.existsById(workOrderId)) {
            throw new EntityNotFoundException("Work order with id " + workOrderId + " not found");
        }
        return componentRepository.findAllByWorkOrder_Id(workOrderId);
    }

    @Override
    public List<WorkOrderComponent> getAllForStep(UUID stepId) {
        if (!stepRepository.existsById(stepId)) {
            throw new EntityNotFoundException("Work order step with id " + stepId + " not found");
        }
        return componentRepository.findAllByStep_Id(stepId);
    }
}
