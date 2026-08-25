package com.github.reck0nerrr.shop.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import com.github.reck0nerrr.shop.entity.Item;

public interface ItemRepository extends JpaRepository<Item,Long> {
    @Query("""
        SELECT DISTINCT i
        FROM Item i
        WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    Page<Item> search(@Param("query") String query, Pageable pageable);
}
