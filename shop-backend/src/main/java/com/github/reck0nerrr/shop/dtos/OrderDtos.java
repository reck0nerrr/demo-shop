package com.github.reck0nerrr.shop.dtos;

import com.github.reck0nerrr.shop.entity.OrderStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class OrderItemRequest {
        private Long itemId;
        private Integer quantity;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateOrderRequest {
        private Long userId;
        private List<OrderItemRequest> items;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class UpdateOrderStatusRequest {
        private OrderStatus status;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderItemResponse {
        private Long itemId;
        private String itemName;
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
