package com.github.reck0nerrr.shop.dtos;

import lombok.*;
import java.time.Instant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public class UserDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateUserRequest {
        @NotBlank
        @Size(min = 3, max = 20)
        private String username;
        @NotBlank
        @Email
        private String email;
        @NotBlank
        @Size(min = 8, max = 64)
        private String password;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private Instant createdAt;
    }
}
