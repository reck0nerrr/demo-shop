package com.github.reck0nerrr.shop.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

public class CartDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class AddCartItemRequest {
        @NotNull
        private Long itemId;
        @NotNull
        @Positive
        private Integer quantity;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class UpdateCartItemRequest {
        @NotNull
        @Min(0)
        private Integer quantity;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CartItemResponse {
        private Long itemId;
        private String itemName;
        private String imageUrl;
        private BigDecimal price;
        private Integer quantity;
        private Integer availableStock;
        private BigDecimal subtotal;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CartResponse {
        private List<CartItemResponse> items;
        private BigDecimal total;
    }
}