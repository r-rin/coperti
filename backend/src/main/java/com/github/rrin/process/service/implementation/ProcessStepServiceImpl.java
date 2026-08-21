package com.github.rrin.process.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.InvalidQuery;
import com.github.rrin.item.Item;
import com.github.rrin.item.repository.ItemRepository;
import com.github.rrin.process.Operation;
import com.github.rrin.process.Process;
import com.github.rrin.process.ProcessStep;
import com.github.rrin.process.dto.ProcessComponentRequest;
import com.github.rrin.process.dto.ProcessStepRequest;
import com.github.rrin.process.repository.OperationRepository;
import com.github.rrin.process.repository.ProcessRepository;
import com.github.rrin.process.repository.ProcessStepRepository;
import com.github.rrin.process.service.ProcessComponentService;
import com.github.rrin.process.service.ProcessStepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProcessStepServiceImpl implements ProcessStepService {

    private final ProcessStepRepository stepRepository;
    private final ProcessRepository processRepository;
    private final OperationRepository operationRepository;
    private final ItemRepository itemRepository;
    private final ProcessComponentService componentService;

    @Autowired
    public ProcessStepServiceImpl(ProcessStepRepository stepRepository,
                                  ProcessRepository processRepository,
                                  OperationRepository operationRepository,
                                  ItemRepository itemRepository,
                                  ProcessComponentService componentService) {
        this.stepRepository = stepRepository;
        this.processRepository = processRepository;
        this.operationRepository = operationRepository;
        this.itemRepository = itemRepository;
        this.componentService = componentService;
    }

    @Override
    public ProcessStep create(UUID processId, ProcessStepRequest request) {
        Process process = processRepository.findById(processId)
                .orElseThrow(() -> new EntityNotFoundException("Process not found with id: " + processId));
        Operation operation = resolveOperation(request.getOperationId());
        Item outputItem = resolveOutputItem(request.getOutputItemId());

        new ValidationCheck()
                .check(request.getOutputQuantity() > 0, "Output quantity must be greater than 0")
                .check(process.getSteps().stream().noneMatch(s -> s.getSeq() == request.getSeq()),
                        "Sequence " + request.getSeq() + " already exists in this process")
                .throwIfAny(InvalidQuery::new);

        ProcessStep step = new ProcessStep();
        step.setProcess(process);
        step.setSeq(request.getSeq());
        step.setOperation(operation);
        step.setOutputItem(outputItem);
        step.setOutputQuantity(request.getOutputQuantity());
        ProcessStep saved = stepRepository.save(step);

        if (request.getComponents() != null) {
            for (ProcessComponentRequest componentRequest : request.getComponents()) {
                componentService.create(saved.getId(), componentRequest);
            }
        }
        return saved;
    }

    @Override
    public ProcessStep update(ProcessStepRequest request) {
        ProcessStep step = checkIfStepExists(request.getId());
        Operation operation = resolveOperation(request.getOperationId());
        Item outputItem = resolveOutputItem(request.getOutputItemId());

        // components are managed via ProcessComponentService, not through step update
        new ValidationCheck()
                .check(request.getOutputQuantity() > 0, "Output quantity must be greater than 0")
                .check(step.getProcess().getSteps().stream()
                                .noneMatch(s -> s.getSeq() == request.getSeq() && !s.getId().equals(step.getId())),
                        "Sequence " + request.getSeq() + " already exists in this process")
                .throwIfAny(InvalidQuery::new);

        step.setSeq(request.getSeq());
        step.setOperation(operation);
        step.setOutputItem(outputItem);
        step.setOutputQuantity(request.getOutputQuantity());
        return stepRepository.save(step);
    }

    @Override
    public ProcessStep get(UUID id) {
        return checkIfStepExists(id);
    }

    @Override
    public ProcessStep delete(UUID id) {
        ProcessStep step = checkIfStepExists(id);
        componentService.deleteAllForStep(id);
        stepRepository.delete(step);
        return step;
    }

    private Operation resolveOperation(UUID operationId) {
        return operationRepository.findById(operationId)
                .orElseThrow(() -> new EntityNotFoundException("Operation not found with id: " + operationId));
    }

    private Item resolveOutputItem(UUID outputItemId) {
        if (outputItemId == null) {
            return null;
        }
        return itemRepository.findById(outputItemId)
                .orElseThrow(() -> new EntityNotFoundException("Output item not found with id: " + outputItemId));
    }

    private ProcessStep checkIfStepExists(UUID id) {
        return stepRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Process step not found with id: " + id));
    }
}
