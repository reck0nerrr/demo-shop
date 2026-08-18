package com.github.reck0nerrr.shop.dtos;


import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

public class ItemDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ItemRequest {
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stockQuantity;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stockQuantity;
        private Instant createdAt;
    }
}
