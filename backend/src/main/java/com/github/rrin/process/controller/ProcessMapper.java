package com.github.rrin.process.controller;

import com.github.rrin.process.Operation;
import com.github.rrin.process.Process;
import com.github.rrin.process.ProcessComponent;
import com.github.rrin.process.ProcessStep;
import com.github.rrin.process.dto.OperationResponse;
import com.github.rrin.process.dto.ProcessComponentResponse;
import com.github.rrin.process.dto.ProcessResponse;
import com.github.rrin.process.dto.ProcessStepResponse;
import com.github.rrin.process.service.ProcessComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProcessMapper {

    private final ProcessComponentService componentService;

    @Autowired
    public ProcessMapper(ProcessComponentService componentService) {
        this.componentService = componentService;
    }

    public ProcessResponse toResponse(Process process) {
        return ProcessResponse.builder()
                .id(process.getId())
                .producedItemId(process.getProduces() != null ? process.getProduces().getId() : null)
                .version(process.getVersion())
                .status(process.getStatus())
                .steps(process.getSteps() == null
                        ? List.of()
                        : process.getSteps().stream().map(this::toResponse).toList())
                .build();
    }

    public ProcessStepResponse toResponse(ProcessStep step) {
        return ProcessStepResponse.builder()
                .id(step.getId())
                .processId(step.getProcess() != null ? step.getProcess().getId() : null)
                .seq(step.getSeq())
                .operationId(step.getOperation() != null ? step.getOperation().getId() : null)
                .outputItemId(step.getOutputItem() != null ? step.getOutputItem().getId() : null)
                .outputQuantity(step.getOutputQuantity())
                .components(componentService.getAllForStep(step.getId()).stream()
                        .map(this::toResponse)
                        .toList())
                .build();
    }

    public ProcessComponentResponse toResponse(ProcessComponent component) {
        return ProcessComponentResponse.builder()
                .id(component.getId())
                .processStepId(component.getProcessStep() != null ? component.getProcessStep().getId() : null)
                .consumedItemId(component.getConsumed() != null ? component.getConsumed().getId() : null)
                .consumableCategoryId(component.getConsumableCategory() != null
                        ? component.getConsumableCategory().getId()
                        : null)
                .consumedQuantity(component.getConsumedQuantity())
                .build();
    }

    public OperationResponse toResponse(Operation operation) {
        return OperationResponse.builder()
                .id(operation.getId())
                .code(operation.getCode())
                .name(operation.getName())
                .description(operation.getDescription())
                .build();
    }
}
