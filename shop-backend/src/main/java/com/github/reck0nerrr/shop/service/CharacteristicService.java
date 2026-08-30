package com.github.reck0nerrr.shop.service;

import com.github.reck0nerrr.shop.dtos.CharacteristicDtos.*;
import com.github.reck0nerrr.shop.entity.CharacteristicType;
import com.github.reck0nerrr.shop.entity.CharacteristicValue;
import com.github.reck0nerrr.shop.repositories.CharacteristicTypeRepository;
import com.github.reck0nerrr.shop.repositories.CharacteristicValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CharacteristicService {

    private final CharacteristicTypeRepository typeRepository;
    private final CharacteristicValueRepository valueRepository;

    @Transactional
    public TypeResponse createType(CreateTypeRequest request) {
        if (typeRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Characteristic type already exists: " + request.getName());
        }
        CharacteristicType type = typeRepository.save(CharacteristicType.builder().name(request.getName()).build());
        return toResponse(type);
    }

    @Transactional(readOnly = true)
    public List<TypeResponse> getAllTypes() {
        return typeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public void deleteType(Long id) {
        if (!typeRepository.existsById(id)) {
            throw new IllegalArgumentException("Characteristic type not found: " + id);
        }
        typeRepository.deleteById(id); // cascades to values; DB rejects if a value is still in use by a variant
    }

    @Transactional
    public ValueResponse addValue(Long typeId, CreateValueRequest request) {
        CharacteristicType type = typeRepository.findById(typeId)
                .orElseThrow(() -> new IllegalArgumentException("Characteristic type not found: " + typeId));
        if (valueRepository.existsByTypeIdAndValueIgnoreCase(typeId, request.getValue())) {
            throw new IllegalArgumentException("Value already exists under this type: " + request.getValue());
        }
        CharacteristicValue value = valueRepository.save(
                CharacteristicValue.builder().type(type).value(request.getValue()).build());
        return ValueResponse.builder().id(value.getId()).value(value.getValue()).build();
    }

    @Transactional
    public void deleteValue(Long valueId) {
        if (!valueRepository.existsById(valueId)) {
            throw new IllegalArgumentException("Characteristic value not found: " + valueId);
        }
        valueRepository.deleteById(valueId); // DB rejects if still referenced by a variant (ON DELETE RESTRICT)
    }

    private TypeResponse toResponse(CharacteristicType type) {
        List<ValueResponse> values = valueRepository.findByTypeId(type.getId()).stream()
                .map(v -> ValueResponse.builder().id(v.getId()).value(v.getValue()).build())
                .toList();
        return TypeResponse.builder().id(type.getId()).name(type.getName()).values(values).build();
    }
}