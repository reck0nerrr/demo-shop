package com.github.reck0nerrr.shop.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.reck0nerrr.shop.entity.Order;

public interface OrderRepository extends JpaRepository<Order,Long>{
    List<Order> findByUserId(Long userId);
}
