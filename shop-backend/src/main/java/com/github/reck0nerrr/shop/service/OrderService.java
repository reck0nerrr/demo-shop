package com.github.reck0nerrr.shop.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.reck0nerrr.shop.entity.*;
import com.github.reck0nerrr.shop.repositories.CartRepository;
import com.github.reck0nerrr.shop.repositories.OrderRepository;
import com.github.reck0nerrr.shop.security.SecurityUtils;
import com.github.reck0nerrr.shop.security.UserPrincipal;
import com.github.reck0nerrr.shop.dtos.OrderDtos.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ItemService itemService;
    private final CartRepository cartRepository;



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
                        .variantId(oi.getVariant().getId())
                        .itemId(oi.getVariant().getItem().getId())
                        .itemName(oi.getVariant().getItem().getName())
                        .characteristics(oi.getVariant().getValues().stream()
                                .collect(Collectors.toMap(cv -> cv.getType().getName(), CharacteristicValue::getValue)))
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

    @Transactional
    public OrderResponse checkout(Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new IllegalStateException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        User user = userService.findUserOrThrow(userId);
        Order order = Order.builder().user(user).status(OrderStatus.PENDING).build();

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            ItemVariant variant = cartItem.getVariant();
            int quantity = cartItem.getQuantity();

            if (quantity > variant.getStockQuantity()) {
                throw new IllegalStateException("Not enough stock for item: " + variant.getItem().getName());
            }

            variant.setStockQuantity(variant.getStockQuantity() - quantity);
            BigDecimal price = variant.effectivePrice();

            orderItems.add(OrderItem.builder()
                    .id(new OrderItemId(null, variant.getId()))
                    .order(order)
                    .variant(variant)
                    .quantity(quantity)
                    .price(price)
                    .build());

            total = total.add(price.multiply(BigDecimal.valueOf(quantity)));
        }

        order.setOrderItems(orderItems);
        order.setTotal(total);
        Order saved = orderRepository.save(order);

        cart.getItems().clear(); // unreached if anything above threw

        return toResponse(saved);
    }
}