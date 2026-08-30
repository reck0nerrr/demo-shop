package com.github.reck0nerrr.shop.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
@Entity
@Table(name="characteristic_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacteristicType {
    @Id
    private Long id;
    private String name;
}
