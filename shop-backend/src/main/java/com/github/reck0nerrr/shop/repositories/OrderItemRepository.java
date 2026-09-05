package com.github.reck0nerrr.shop.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.reck0nerrr.shop.entity.OrderItem;
import com.github.reck0nerrr.shop.entity.OrderItemId;

public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
    List<OrderItem> findByOrderId(Long orderId);
    @Query("SELECT DISTINCT oi.variant.id FROM OrderItem oi WHERE oi.variant.id IN :variantIds")
    List<Long> findVariantIdsInUse(@Param("variantIds") List<Long> variantIds);
}