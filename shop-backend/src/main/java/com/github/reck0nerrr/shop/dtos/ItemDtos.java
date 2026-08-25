package com.github.reck0nerrr.shop.dtos;


import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
public class ItemDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ItemRequest {
        @NotBlank
        @Size(min = 1,max=100)
        private String name;
        private String description;
        @NotNull
        @DecimalMin(value = "0.00")
        @Digits(integer = 12, fraction = 2)
        private BigDecimal price;
        @NotNull
        @Min(value = 0)
        private Integer stockQuantity;
        
        private List<String> imageUrls;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stockQuantity;
        private List<String> imageUrls;
        private Instant createdAt;
    }
}
