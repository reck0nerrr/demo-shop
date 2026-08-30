package com.github.reck0nerrr.shop.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;
@Entity
@Table(name="characteristic_values")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacteristicValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "characteristic_type_id", nullable = false)
    private CharacteristicType type;
    private String value;
}
