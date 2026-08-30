package com.github.reck0nerrr.shop.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.reck0nerrr.shop.dtos.PageResponse;
import com.github.reck0nerrr.shop.dtos.ItemDtos.ItemRequest;
import com.github.reck0nerrr.shop.dtos.ItemDtos.ItemResponse;
import com.github.reck0nerrr.shop.dtos.ItemDtos.UpdateVariantsRequest;
import com.github.reck0nerrr.shop.dtos.ItemDtos.VariantRequest;
import com.github.reck0nerrr.shop.dtos.ItemDtos.VariantResponse;
import com.github.reck0nerrr.shop.entity.CharacteristicType;
import com.github.reck0nerrr.shop.entity.CharacteristicValue;
import com.github.reck0nerrr.shop.entity.Item;
import com.github.reck0nerrr.shop.entity.ItemImage;
import com.github.reck0nerrr.shop.entity.ItemVariant;
import com.github.reck0nerrr.shop.repositories.CharacteristicTypeRepository;
import com.github.reck0nerrr.shop.repositories.CharacteristicValueRepository;
import com.github.reck0nerrr.shop.repositories.ItemRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CharacteristicTypeRepository typeRepository;
    private final CharacteristicValueRepository valueRepository;
    @Transactional
    public ItemResponse create(ItemRequest request) {
        Item item = Item.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .build();
        item.setImages(buildImages(item, request.getImageUrls()));
        item.setCharacteristicTypes(resolveTypes(request.getCharacteristicTypeIds()));
        Item saved = itemRepository.save(item);
        saved.getVariants().add(ItemVariant.builder().item(saved).stockQuantity(0).build());
        return toResponse(saved);
    }
    Set<CharacteristicType> resolveTypes(List<Long> typeIds) {
        if (typeIds == null || typeIds.isEmpty()) return new HashSet<>();
        List<CharacteristicType> types = typeRepository.findAllById(typeIds);
        if (types.size() != typeIds.size()) {
            throw new IllegalArgumentException("one or more characteristic types not found");
        }
        return new HashSet<>(types);
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
        item.getImages().clear();
        item.getImages().addAll(buildImages(item, request.getImageUrls()));
        item.setCharacteristicTypes(resolveTypes(request.getCharacteristicTypeIds()));
        return toResponse(item);
    }
    @Transactional
    public ItemResponse replaceVariants(Long id, UpdateVariantsRequest request){
        Item item = findItemOrThrow(id);
        Set<Long> activeTypeIds=item.getCharacteristicTypes().stream()
            .map(CharacteristicType::getId).collect(Collectors.toSet());
        List<ItemVariant> newVariants = new ArrayList<>();
        Set<Set<Long>> seenCombinations = new HashSet<>();
        for(VariantRequest vr : request.getVariants()){
            List<CharacteristicValue> values = valueRepository.findAllById(vr.getCharacteristicValueIds());
            if (values.size() != vr.getCharacteristicValueIds().size()) {
                throw new IllegalArgumentException("one or more characteristic values not found");
            }
            for (CharacteristicValue v : values) {
                if (!activeTypeIds.contains(v.getType().getId())) {
                    throw new IllegalArgumentException(
                            "value \"" + v.getValue() + "\" belongs to a characteristic type not enabled on this item");
                }
            }

            Set<Long> combo = values.stream().map(CharacteristicValue::getId).collect(Collectors.toSet());
            if (!seenCombinations.add(combo)) {
                throw new IllegalArgumentException("Duplicate variant combination submitted");
            }

            newVariants.add(ItemVariant.builder()
                    .item(item)
                    .stockQuantity(vr.getStockQuantity())
                    .priceOverride(vr.getPriceOverride())
                    .values(new HashSet<>(values))
                    .build());
        }
        if (newVariants.isEmpty()) {
            newVariants.add(ItemVariant.builder().item(item).stockQuantity(0).build());
        }

        item.getVariants().clear();
        item.getVariants().addAll(newVariants);
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
        List<String> imageUrls = item.getImages().stream().map(ItemImage::getImageUrl).toList();

        List<VariantResponse> variants = item.getVariants().stream()
                .map(v -> VariantResponse.builder()
                        .id(v.getId())
                        .stockQuantity(v.getStockQuantity())
                        .price(v.effectivePrice())
                        .priceOverride(v.getPriceOverride())
                        .characteristics(v.getValues().stream()
                                .collect(Collectors.toMap(cv -> cv.getType().getName(), CharacteristicValue::getValue)))
                        .build())
                .toList();

        int totalStock = item.getVariants().stream().mapToInt(ItemVariant::getStockQuantity).sum();
        List<String> typeNames = item.getCharacteristicTypes().stream().map(CharacteristicType::getName).toList();

        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .totalStock(totalStock)
                .imageUrls(imageUrls)
                .characteristicTypes(typeNames)
                .variants(variants)
                .createdAt(item.getCreatedAt())
                .build();
    }
}
