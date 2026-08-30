package com.github.reck0nerrr.shop.controllers;

import com.github.reck0nerrr.shop.dtos.CharacteristicDtos.*;
import com.github.reck0nerrr.shop.service.CharacteristicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/characteristics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CharacteristicController {

    private final CharacteristicService characteristicService;

    @GetMapping
    public ResponseEntity<List<TypeResponse>> getAll() {
        return ResponseEntity.ok(characteristicService.getAllTypes());
    }

    @PostMapping
    public ResponseEntity<TypeResponse> createType(@Valid @RequestBody CreateTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(characteristicService.createType(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteType(@PathVariable Long id) {
        characteristicService.deleteType(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{typeId}/values")
    public ResponseEntity<ValueResponse> addValue(@PathVariable Long typeId, @Valid @RequestBody CreateValueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(characteristicService.addValue(typeId, request));
    }

    @DeleteMapping("/values/{valueId}")
    public ResponseEntity<Void> deleteValue(@PathVariable Long valueId) {
        characteristicService.deleteValue(valueId);
        return ResponseEntity.noContent().build();
    }
}