package com.buyme.auth;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TokenService {

    public String generateToken(Long userId, String role) {
        return userId + "." + role + "." + UUID.randomUUID();
    }
}
