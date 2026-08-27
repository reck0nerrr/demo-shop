package com.github.reck0nerrr.shop.repositories;

import com.github.reck0nerrr.shop.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("""
        SELECT c FROM Cart c
        LEFT JOIN FETCH c.items ci
        LEFT JOIN FETCH ci.item
        WHERE c.user.id = :userId
    """)
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);
}