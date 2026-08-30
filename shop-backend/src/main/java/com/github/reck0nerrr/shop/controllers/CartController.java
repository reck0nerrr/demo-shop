package com.github.reck0nerrr.shop.controllers;


import com.github.reck0nerrr.shop.dtos.CartDtos.*;
import com.github.reck0nerrr.shop.security.UserPrincipal;
import com.github.reck0nerrr.shop.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartService.getCart(principal.getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @Valid @RequestBody AddCartItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(cartService.addItem(principal.getId(), request));
    }

    @PatchMapping("/items/{variantId}")
    public ResponseEntity<CartResponse> updateQuantity(
            @PathVariable Long variantId,
            @Valid @RequestBody UpdateCartItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(cartService.updateQuantity(principal.getId(), variantId, request));
    }

    @DeleteMapping("/items/{variantId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable Long variantId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(cartService.removeItem(principal.getId(), variantId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        cartService.clearCart(principal.getId());
        return ResponseEntity.noContent().build();
    }

}
