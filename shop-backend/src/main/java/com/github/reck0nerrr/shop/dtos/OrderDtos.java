package com.github.reck0nerrr.shop.dtos;

import com.github.reck0nerrr.shop.entity.OrderStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class OrderDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull
        private Long itemId;
        @NotNull
        @Positive
        private Integer quantity;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateOrderRequest {
        @NotEmpty
        @Valid
        private List<OrderItemRequest> items;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class UpdateOrderStatusRequest {
        @NotNull
        private OrderStatus status;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderItemResponse {
        private Long variantId;
        private String itemName;
        private Map<String, String> characteristics;
        private Integer quantity;
        private BigDecimal price;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderResponse {
        private Long id;
        private Long userId;
        private OrderStatus status;
        private Instant createdAt;
        private List<OrderItemResponse> items;
        private BigDecimal total;
    }

}
