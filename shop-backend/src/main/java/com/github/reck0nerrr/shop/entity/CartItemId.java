package com.github.reck0nerrr.shop.entity;

import lombok.*;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemId implements Serializable {

    private Long cartId;
    private Long variantId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartItemId that)) return false;
        return Objects.equals(cartId, that.cartId) && Objects.equals(variantId, that.variantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartId, variantId);
    }
}
