package com.github.reck0nerrr.shop.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.reck0nerrr.shop.entity.CharacteristicValue;

public interface CharacteristicValueRepository extends JpaRepository<CharacteristicValue,Long>{

    boolean existsByTypeIdAndValueIgnoreCase(Long typeId, String value);

	List<CharacteristicValue> findByTypeId(Long typeId);
    
}
