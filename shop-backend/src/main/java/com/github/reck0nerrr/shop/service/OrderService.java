package com.github.reck0nerrr.shop.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.reck0nerrr.shop.entity.*;
import com.github.reck0nerrr.shop.repositories.OrderRepository;
import com.github.reck0nerrr.shop.security.SecurityUtils;
import com.github.reck0nerrr.shop.security.UserPrincipal;
import com.github.reck0nerrr.shop.dtos.OrderDtos.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, Long currentUserId) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        User user = userService.findUserOrThrow(currentUserId);

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest line : request.getItems()) {
            if (line.getQuantity() == null || line.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive for item " + line.getItemId());
            }

            Item item = itemService.findItemOrThrow(line.getItemId());

            if (item.getStockQuantity() < line.getQuantity()) {
                throw new IllegalStateException("Not enough stock for item: " + item.getName());
            }

            item.setStockQuantity(item.getStockQuantity() - line.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .id(new OrderItemId(null, item.getId()))
                    .order(order)
                    .item(item)
                    .quantity(line.getQuantity())
                    .price(item.getPrice())
                    .build();

            orderItems.add(orderItem);
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
        }

        order.setOrderItems(orderItems);
        order.setTotal(total);
        Order saved = orderRepository.save(order);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id, UserPrincipal currentUser) {
        Order order = findOrderOrThrow(id);
        if (!order.getUser().getId().equals(currentUser.getId()) && !SecurityUtils.isAdmin(currentUser)) {
            throw new AccessDeniedException("You do not have access to this order");
        }
        return toResponse(findOrderOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getByUser(Long userId) {
        return orderRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAll() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = findOrderOrThrow(id);
        order.setStatus(status);
        return toResponse(order);
    }

    private Order findOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(oi -> OrderItemResponse.builder()
                        .itemId(oi.getItem().getId())
                        .itemName(oi.getItem().getName())
                        .quantity(oi.getQuantity())
                        .price(oi.getPrice())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .items(items)
                .total(order.getTotal())
                .build();
    }
}