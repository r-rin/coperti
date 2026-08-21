package com.github.rrin.process.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.InvalidQuery;
import com.github.rrin.item.repository.ItemCategoryRepository;
import com.github.rrin.item.repository.ItemRepository;
import com.github.rrin.process.ProcessComponent;
import com.github.rrin.process.ProcessStep;
import com.github.rrin.process.dto.ProcessComponentRequest;
import com.github.rrin.process.repository.ProcessComponentRepository;
import com.github.rrin.process.repository.ProcessStepRepository;
import com.github.rrin.process.service.ProcessComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProcessComponentServiceImpl implements ProcessComponentService {

    private final ProcessComponentRepository componentRepository;
    private final ProcessStepRepository stepRepository;
    private final ItemRepository itemRepository;
    private final ItemCategoryRepository categoryRepository;

    @Autowired
    public ProcessComponentServiceImpl(ProcessComponentRepository componentRepository,
                                       ProcessStepRepository stepRepository,
                                       ItemRepository itemRepository,
                                       ItemCategoryRepository categoryRepository) {
        this.componentRepository = componentRepository;
        this.stepRepository = stepRepository;
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public ProcessComponent create(UUID processStepId, ProcessComponentRequest request) {
        ProcessStep step = stepRepository.findById(processStepId)
                .orElseThrow(() -> new EntityNotFoundException("Process step not found with id: " + processStepId));

        validateConsumable(request);

        ProcessComponent component = new ProcessComponent();
        component.setProcessStep(step);
        applyConsumable(component, request);
        component.setConsumedQuantity(request.getConsumedQuantity());
        return componentRepository.save(component);
    }

    @Override
    public ProcessComponent update(ProcessComponentRequest request) {
        ProcessComponent component = checkIfComponentExists(request.getId());
        validateConsumable(request);
        applyConsumable(component, request);
        component.setConsumedQuantity(request.getConsumedQuantity());
        return componentRepository.save(component);
    }

    @Override
    public ProcessComponent get(UUID id) {
        return checkIfComponentExists(id);
    }

    @Override
    public ProcessComponent delete(UUID id) {
        ProcessComponent component = checkIfComponentExists(id);
        componentRepository.delete(component);
        return component;
    }

    @Override
    public List<ProcessComponent> deleteAllForStep(UUID processStepId) {
        List<ProcessComponent> components = componentRepository.findAllByProcessStep_Id(processStepId);
        componentRepository.deleteAll(components);
        return components;
    }

    private void validateConsumable(ProcessComponentRequest request) {
        boolean itemSet = request.getConsumedItemId() != null;
        boolean categorySet = request.getConsumableCategoryId() != null;

        new ValidationCheck()
                .check(itemSet ^ categorySet, "Exactly one of consumedItemId or consumableCategoryId must be set")
                .check(request.getConsumedQuantity() > 0, "Consumed quantity must be greater than 0")
                .throwIfAny(InvalidQuery::new);

        new ValidationCheck()
                .check(!itemSet || itemRepository.existsById(request.getConsumedItemId()),
                        "Consumed item not found with id: " + request.getConsumedItemId())
                .check(!categorySet || categoryRepository.existsById(request.getConsumableCategoryId()),
                        "Consumable category not found with id: " + request.getConsumableCategoryId())
                .throwIfAny(EntityNotFoundException::new);
    }

    private void applyConsumable(ProcessComponent component, ProcessComponentRequest request) {
        component.setConsumed(request.getConsumedItemId() == null
                ? null
                : itemRepository.findById(request.getConsumedItemId()).orElse(null));
        component.setConsumableCategory(request.getConsumableCategoryId() == null
                ? null
                : categoryRepository.findById(request.getConsumableCategoryId()).orElse(null));
    }

    private ProcessComponent checkIfComponentExists(UUID id) {
        return componentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Process component not found with id: " + id));
    }
}
