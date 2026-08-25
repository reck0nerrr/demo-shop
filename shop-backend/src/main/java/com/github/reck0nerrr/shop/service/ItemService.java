package com.github.reck0nerrr.shop.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.reck0nerrr.shop.dtos.PageResponse;
import com.github.reck0nerrr.shop.dtos.ItemDtos.ItemRequest;
import com.github.reck0nerrr.shop.dtos.ItemDtos.ItemResponse;
import com.github.reck0nerrr.shop.entity.Item;
import com.github.reck0nerrr.shop.entity.ItemImage;
import com.github.reck0nerrr.shop.repositories.ItemRepository;

import java.util.ArrayList;
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
                .build();
        item.setImages(buildImages(item, request.getImageUrls()));
        return toResponse(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public ItemResponse getById(Long id) {
        return toResponse(findItemOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ItemResponse> getAll(Pageable pageable) {
        return PageResponse.of(itemRepository.findAll(pageable).map(this::toResponse));
    }
    @Transactional(readOnly = true)
    public PageResponse<ItemResponse> search(String query, Pageable pageable) {
        return PageResponse.of(itemRepository.search(query, pageable).map(this::toResponse));
    }
    @Transactional
    public ItemResponse update(Long id, ItemRequest request) {
        Item item = findItemOrThrow(id);
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        if (request.getStockQuantity() != null) {
            item.setStockQuantity(request.getStockQuantity());
        }
        item.getImages().clear();
        item.getImages().addAll(buildImages(item, request.getImageUrls()));
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

    private List<ItemImage> buildImages(Item item, List<String> urls) {
        List<ItemImage> images = new ArrayList<>();
        if (urls == null) return images;

        int order = 0;
        for (String url : urls) {
            if (url == null || url.isBlank()) continue;
            images.add(ItemImage.builder().item(item).imageUrl(url).sortOrder(order++).build());
        }
        return images;
    }
        
    private ItemResponse toResponse(Item item) {
        List<String> imageUrls = item.getImages().stream()
                .map(ItemImage::getImageUrl)
                .toList();
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .stockQuantity(item.getStockQuantity())
                .imageUrls(imageUrls)
                .createdAt(item.getCreatedAt())
                .build();
    }
}
