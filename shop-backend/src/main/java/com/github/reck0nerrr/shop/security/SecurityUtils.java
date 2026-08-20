package com.github.reck0nerrr.shop.security;

import org.springframework.security.access.AccessDeniedException;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static boolean isAdmin(UserPrincipal principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public static void requireSelfOrAdmin(UserPrincipal principal, Long targetUserId) {
        if (!principal.getId().equals(targetUserId) && !isAdmin(principal)) {
            throw new AccessDeniedException("no access for you");
        }
    }
}