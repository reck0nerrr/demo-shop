package com.github.reck0nerrr.shop;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.reck0nerrr.shop.dtos.ItemDtos.VariantRequest;

class VariantRequestValidationTest {
    private final Validator validator;
    VariantRequestValidationTest(){
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator=factory.getValidator();
    }

    @Test 
    void plainItemVariantWithNoCharacteristicsIsValid() {
        // a plain item's Default variant legitimately has zero characteristic
        // values — this must NOT be rejected (regression guard for the bug
        // where @NotEmpty here caused every plain-item variant save to 400)
        VariantRequest request = new VariantRequest();
        request.setStockQuantity(5);
        request.setPriceOverride(null);
        request.setCharacteristicValueIds(Collections.emptyList());

        Set<ConstraintViolation<VariantRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty(),
                "an empty characteristicValueIds list must be valid — violations: " + violations);
    }

    @Test
    void negativeStockIsRejected() {
        VariantRequest request = new VariantRequest();
        request.setStockQuantity(-1);
        request.setCharacteristicValueIds(Collections.emptyList());

        Set<ConstraintViolation<VariantRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("stockQuantity")),
                "negative stock should still be rejected");
    }

}
