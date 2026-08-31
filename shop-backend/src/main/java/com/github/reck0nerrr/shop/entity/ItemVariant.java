package com.github.reck0nerrr.shop.entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.BatchSize;


import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name = "item_variants")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "item_id",nullable = false)
    private Item item;
    @Builder.Default
    private Integer stockQuantity = 0;
    private BigDecimal priceOverride;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "item_variant_values",
        joinColumns = @JoinColumn(name = "variant_id"),
        inverseJoinColumns = @JoinColumn(name = "characteristic_value_id")
    )
    @BatchSize(size = 20)
    @Builder.Default
    private Set<CharacteristicValue> values = new HashSet<>();

    public BigDecimal effectivePrice() {
        return priceOverride != null ? priceOverride : item.getPrice();
    }
}
