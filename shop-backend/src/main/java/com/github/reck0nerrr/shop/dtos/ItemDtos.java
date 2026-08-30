package com.github.reck0nerrr.shop.dtos;


import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
public class ItemDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ItemRequest {
        @NotBlank
        @Size(min = 1,max=255)
        private String name;
        private String description;
        @NotNull
        @DecimalMin(value = "0.0")
        private BigDecimal price;
        private List<String> imageUrls;
        private List<Long> characteristicTypeIds;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class VariantRequest {
        private Long id;
        @NotNull @Min(0)
        private Integer stockQuantity;
        @DecimalMin("0.0")
        private BigDecimal priceOverride;
        @NotEmpty
        private List<Long> characteristicValueIds;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class UpdateVariantsRequest {
        @Valid @NotNull
        private List<VariantRequest> variants;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class VariantResponse {
        private Long id;
        private Integer stockQuantity;
        private BigDecimal price;
        private BigDecimal priceOverride;
        private Map<String, String> characteristics;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer totalStock;
        private List<String> imageUrls;
        private List<String> characteristicTypes;
        private List<VariantResponse> variants;
        private Instant createdAt;
    }
}
