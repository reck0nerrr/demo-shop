package com.github.reck0nerrr.shop.service;

import com.github.reck0nerrr.shop.dtos.CartDtos.*;
import com.github.reck0nerrr.shop.entity.*;
import com.github.reck0nerrr.shop.repositories.CartRepository;
import com.github.reck0nerrr.shop.repositories.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Transactional
    public CartResponse getCart(Long userId) {
        return toResponse(getOrCreateCart(userId));
    }

    @Transactional
    public CartResponse addItem(Long userId, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + request.getItemId()));

        CartItem existing = findLine(cart, item.getId());
        int newQuantity = (existing != null ? existing.getQuantity() : 0) + request.getQuantity();

        if (newQuantity > item.getStockQuantity()) {
            throw new IllegalStateException("Only " + item.getStockQuantity() + " of \"" + item.getName() + "\" in stock");
        }

        if (existing != null) {
            existing.setQuantity(newQuantity);
        } else {
            cart.getItems().add(CartItem.builder()
                    .id(new CartItemId(cart.getId(), item.getId()))
                    .cart(cart)
                    .item(item)
                    .quantity(newQuantity)
                    .build());
        }

        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateQuantity(Long userId, Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        CartItem line = findLine(cart, itemId);
        if (line == null) {
            throw new IllegalArgumentException("Item not in cart: " + itemId);
        }

        int quantity = request.getQuantity();
        if (quantity == 0) {
            cart.getItems().remove(line);
        } else {
            if (quantity > line.getItem().getStockQuantity()) {
                throw new IllegalStateException(
                        "Only " + line.getItem().getStockQuantity() + " of \"" + line.getItem().getName() + "\" in stock");
            }
            line.setQuantity(quantity);
        }

        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().removeIf(ci -> ci.getItem().getId().equals(itemId));
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

    private CartItem findLine(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(ci -> ci.getItem().getId().equals(itemId))
                .findFirst()
                .orElse(null);
    }

    private CartResponse toResponse(Cart cart) {
        var items = cart.getItems().stream()
                .map(ci -> CartItemResponse.builder()
                        .itemId(ci.getItem().getId())
                        .itemName(ci.getItem().getName())
                        .imageUrl(ci.getItem().getImages().isEmpty() ? null : ci.getItem().getImages().get(0).getImageUrl())
                        .price(ci.getItem().getPrice())
                        .quantity(ci.getQuantity())
                        .availableStock(ci.getItem().getStockQuantity())
                        .subtotal(ci.getItem().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                        .build())
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder().items(items).total(total).build();
    }
}
