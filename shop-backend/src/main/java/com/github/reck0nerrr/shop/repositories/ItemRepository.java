package com.github.reck0nerrr.shop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.reck0nerrr.shop.entity.Item;

public interface ItemRepository extends JpaRepository<Item,Long> {
    
}
