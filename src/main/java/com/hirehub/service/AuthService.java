package com.hirehub.service;

import com.hirehub.dto.auth.AuthResponse;
import com.hirehub.dto.auth.LoginRequest;
import com.hirehub.dto.auth.RegisterRequest;
import com.hirehub.exception.DuplicateResourceException;
import com.hirehub.model.Role;
import com.hirehub.model.User;
import com.hirehub.repository.UserRepository;
import com.hirehub.security.CustomUserDetails;
import com.hirehub.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user registration and JWT-based authentication.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    // ── Register ─────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email already registered: " + request.getEmail());
        }

        // Prevent self-assignment of ADMIN role unless it's the first user
        Role assignedRole = sanitizeRole(request.getRole());

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(assignedRole)
                .enabled(true)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {} [{}]", user.getEmail(), user.getRole());

        String token = jwtTokenProvider.generateTokenFromEmail(user.getEmail(), user.getId());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(userDetails.getEmail())
                .orElseThrow();

        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Public API users may not self-assign ADMIN. Defaults to CANDIDATE if null.
     */
    private Role sanitizeRole(Role requested) {
        if (requested == null) return Role.ROLE_CANDIDATE;
        if (requested == Role.ROLE_ADMIN) return Role.ROLE_CANDIDATE;
        return requested;
    }
}
