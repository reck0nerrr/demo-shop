package com.github.reck0nerrr.shop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.reck0nerrr.shop.entity.ItemVariant;

public interface ItemVariantRepository extends JpaRepository<ItemVariant, Long>{
    
}
