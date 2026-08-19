package com.github.reck0nerrr.shop.config;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.*;
@Component
@RequiredArgsConstructor
public class JwtTest {

    private final JwtProperties jwtProperties;

    @PostConstruct
    void test() {
        System.out.println(jwtProperties.getExpirationMs());
    }
}
