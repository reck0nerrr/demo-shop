package com.github.reck0nerrr.shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.reck0nerrr.shop.dtos.ItemDtos.ItemRequest;
import com.github.reck0nerrr.shop.dtos.ItemDtos.ItemResponse;
import com.github.reck0nerrr.shop.entity.Item;
import com.github.reck0nerrr.shop.repositories.ItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public ItemResponse create(ItemRequest request) {
        Item item = Item.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .imageUrl(request.getImageUrl())
                .build();
        return toResponse(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public ItemResponse getById(Long id) {
        return toResponse(findItemOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getAll() {
        return itemRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public ItemResponse update(Long id, ItemRequest request) {
        Item item = findItemOrThrow(id);
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setImageUrl(request.getImageUrl());
        if (request.getStockQuantity() != null) {
            item.setStockQuantity(request.getStockQuantity());
        }
        return toResponse(item);
    }

    @Transactional
    public void delete(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new IllegalArgumentException("Item not found: " + id);
        }
        itemRepository.deleteById(id);
    }

    Item findItemOrThrow(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + id));
    }

    private ItemResponse toResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .stockQuantity(item.getStockQuantity())
                .imageUrl(item.getImageUrl())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
