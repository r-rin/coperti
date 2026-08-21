package com.github.rrin.item.service.implementation;

import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.item.Item;
import com.github.rrin.item.ItemCategory;
import com.github.rrin.item.ItemType;
import com.github.rrin.item.dto.ItemRequest;
import com.github.rrin.item.repository.ItemRepository;
import com.github.rrin.item.service.ItemCategoryService;
import com.github.rrin.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemCategoryService itemCategoryService;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, ItemCategoryService itemCategoryService) {
        this.itemRepository = itemRepository;
        this.itemCategoryService = itemCategoryService;
    }

    @Override
    public Item create(ItemRequest itemRequest) {
        Item item = new Item();
        item.setName(itemRequest.getName());
        item.setType(itemRequest.getType() == null ? ItemType.PART : itemRequest.getType());

        if (itemRequest.getCategoryId() == null) {
            return itemRepository.save(item);

        }
        ItemCategory category = itemCategoryService.get(itemRequest.getCategoryId());
        item.setCategory(category);

        return itemRepository.save(item);
    }

    @Override
    public Item update(ItemRequest itemRequest) {
        Item item = checkIfItemExists(itemRequest.getId());
        item.setName(itemRequest.getName());
        item.setType(itemRequest.getType());

        if (itemRequest.getCategoryId() == null) {
            return itemRepository.save(item);
        }
        ItemCategory category = itemCategoryService.get(itemRequest.getCategoryId());
        item.setCategory(category);

        return itemRepository.save(item);
    }

    @Override
    public Item get(UUID id) {
        return checkIfItemExists(id);
    }

    @Override
    public Item delete(UUID id) {
        Item item = checkIfItemExists(id);
        itemRepository.delete(item);
        return item;
    }

    @Override
    public Item addItemToCategory(UUID itemId, UUID categoryId) {
        Item item = checkIfItemExists(itemId);
        ItemCategory category = itemCategoryService.get(categoryId);

        item.setCategory(category);

        return itemRepository.save(item);
    }

    @Override
    public Item removeItemFromCategory(UUID itemId, UUID categoryId) {
        Item item = checkIfItemExists(itemId);
        item.setCategory(null);
        return itemRepository.save(item);
    }

    @Override
    public Item setItemType(UUID itemId, ItemType type) {
        Item item = checkIfItemExists(itemId);
        item.setType(type);
        return itemRepository.save(item);
    }

    @Override
    public List<Item> getItemsByCategory(UUID categoryId) {
        ItemCategory category = itemCategoryService.get(categoryId);
        return itemRepository.findByCategory(category);
    }

    @Override
    public List<Item> getItemsByType(ItemType type) {
        return itemRepository.findByType(type);
    }

    private Item checkIfItemExists(UUID id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item with id " + id + " not found"));
    }
}
