package com.buyme.auth;

public record AuthResponse(
        Long userId,
        String username,
        String role,
        String token
) {
}
