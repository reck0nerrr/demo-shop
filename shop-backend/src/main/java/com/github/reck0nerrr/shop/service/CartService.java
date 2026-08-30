package com.github.reck0nerrr.shop.service;

import com.github.reck0nerrr.shop.dtos.CartDtos.*;
import com.github.reck0nerrr.shop.entity.*;
import com.github.reck0nerrr.shop.repositories.CartRepository;
import com.github.reck0nerrr.shop.repositories.ItemRepository;
import com.github.reck0nerrr.shop.repositories.ItemVariantRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ItemVariantRepository variantRepository;
    private final UserService userService;

    @Transactional
    public CartResponse getCart(Long userId) {
        return toResponse(getOrCreateCart(userId));
    }

    @Transactional
    public CartResponse addItem(Long userId, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        ItemVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + request.getVariantId()));

        CartItem existing = findLine(cart, variant.getId());
        int newQuantity = (existing != null ? existing.getQuantity() : 0) + request.getQuantity();

        if (newQuantity > variant.getStockQuantity()) {
            throw new IllegalStateException(
                    "Only " + variant.getStockQuantity() + " of \"" + variant.getItem().getName() + "\" in stock");
        }

        if (existing != null) {
            existing.setQuantity(newQuantity);
        } else {
            cart.getItems().add(CartItem.builder()
                    .id(new CartItemId(cart.getId(), variant.getId()))
                    .cart(cart)
                    .variant(variant)
                    .quantity(newQuantity)
                    .build());
        }

        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateQuantity(Long userId, Long variantId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        CartItem line = findLine(cart, variantId);
        if (line == null) {
            throw new IllegalArgumentException("Variant not in cart: " + variantId);
        }

        int quantity = request.getQuantity();
        if (quantity == 0) {
            cart.getItems().remove(line);
        } else {
            if (quantity > line.getVariant().getStockQuantity()) {
                throw new IllegalStateException(
                        "Only " + line.getVariant().getStockQuantity() + " of \"" + line.getVariant().getItem().getName() + "\" in stock");
            }
            line.setQuantity(quantity);
        }

        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long variantId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().removeIf(ci -> ci.getVariant().getId().equals(variantId));
        return toResponse(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        getOrCreateCart(userId).getItems().clear();
    }

    Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> {
                    User user = userService.findUserOrThrow(userId);
                    return cartRepository.save(Cart.builder().user(user).build());
                });
    }

    private CartItem findLine(Cart cart, Long variantId) {
        return cart.getItems().stream()
                .filter(ci -> ci.getVariant().getId().equals(variantId))
                .findFirst()
                .orElse(null);
    }

    private CartResponse toResponse(Cart cart) {
        var items = cart.getItems().stream()
                .map(ci -> {
                    ItemVariant v = ci.getVariant();
                    Item item = v.getItem();
                    return CartItemResponse.builder()
                            .variantId(v.getId())
                            .itemId(item.getId())
                            .itemName(item.getName())
                            .imageUrl(item.getImages().isEmpty() ? null : item.getImages().get(0).getImageUrl())
                            .characteristics(v.getValues().stream()
                                    .collect(Collectors.toMap(cv -> cv.getType().getName(), CharacteristicValue::getValue)))
                            .price(v.effectivePrice())
                            .quantity(ci.getQuantity())
                            .availableStock(v.getStockQuantity())
                            .subtotal(v.effectivePrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                            .build();
                })
                .toList();

        BigDecimal total = items.stream().map(CartItemResponse::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return CartResponse.builder().items(items).total(total).build();
    }
}
