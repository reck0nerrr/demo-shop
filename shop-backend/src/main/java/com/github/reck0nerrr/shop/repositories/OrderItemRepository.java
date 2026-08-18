package com.github.reck0nerrr.shop.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.reck0nerrr.shop.entity.OrderItem;
import com.github.reck0nerrr.shop.entity.OrderItemId;

public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
    List<OrderItem> findByOrderId(Long orderId);
}