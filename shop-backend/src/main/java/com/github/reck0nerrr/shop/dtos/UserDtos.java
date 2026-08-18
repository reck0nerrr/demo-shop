package com.github.reck0nerrr.shop.dtos;

import lombok.*;
import java.time.Instant;

public class UserDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateUserRequest {
        private String username;
        private String email;
        private String passwordHash;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private Instant createdAt;
    }
}
