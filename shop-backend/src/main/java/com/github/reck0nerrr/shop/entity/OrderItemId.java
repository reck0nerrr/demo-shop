package com.github.reck0nerrr.shop.entity;

import lombok.*;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemId implements Serializable {

    private Long orderId;
    private Long variantId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItemId that)) return false;
        return Objects.equals(orderId, that.orderId) && Objects.equals(variantId, that.variantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, variantId);
    }
}