package com.buyme.auth;

import com.buyme.user.Role;
import com.buyme.user.User;
import com.buyme.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository,
                       PasswordService passwordService,
                       TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordService.hash(request.password()));
        user.setEmail(request.email());
        user.setRole(Role.END_USER);
        user.setCreatedAt(Instant.now());
        user.setActive(true);

        userRepository.save(user);

        String token = tokenService.generateToken(user.getId(), user.getRole().name());
        return new AuthResponse(user.getId(), user.getUsername(), user.getRole().name(), token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordService.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String token = tokenService.generateToken(user.getId(), user.getRole().name());
        return new AuthResponse(user.getId(), user.getUsername(), user.getRole().name(), token);
    }

    public void logout(Long userId) {
        // No-op for now
    }
}
