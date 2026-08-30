package com.github.reck0nerrr.shop.dtos;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class CharacteristicDtos {
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateTypeRequest {
        @NotBlank @Size(max = 50)
        private String name;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateValueRequest {
        @NotBlank @Size(max = 50)
        private String value;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ValueResponse {
        private Long id;
        private String value;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TypeResponse {
        private Long id;
        private String name;
        private List<ValueResponse> values;
    }
}
