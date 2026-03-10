package com.shopnest.service;

import com.shopnest.dto.request.LoginRequest;
import com.shopnest.dto.request.RegisterRequest;
import com.shopnest.dto.response.AuthResponse;
import com.shopnest.entity.UserEntity;
import com.shopnest.entity.UserRole;
import com.shopnest.exception.DuplicateResourceException;
import com.shopnest.exception.ResourceNotFoundException;
import com.shopnest.repository.UserRepository;
import com.shopnest.security.CustomUserDetailsService;
import com.shopnest.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    // ── REGISTER ──────────────────────────────────────
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Check duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "User", "email", request.getEmail());
        }

        // Create user with BCrypt hashed password
        UserEntity user = UserEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(UserRole.CUSTOMER)    // default role on register
                .active(true)
                .build();

        UserEntity saved = userRepository.save(user);
        log.debug("Registered new user: {}", saved.getEmail());

        // Generate tokens
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(saved.getEmail());
        String accessToken  = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return buildAuthResponse(saved, accessToken, refreshToken);
    }

    // ── LOGIN ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        // AuthenticationManager validates email + password
        // Throws AuthenticationException if invalid
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        // If we reach here — credentials are valid
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", request.getEmail()));

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken  = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.debug("User logged in: {}", user.getEmail());
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // ── REFRESH TOKEN ─────────────────────────────────
    @Transactional(readOnly = true)
    public AuthResponse refresh(String refreshToken) {

        // Extract email from refresh token
        String email = jwtService.extractEmail(refreshToken);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", email));

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(email);

        // Validate refresh token
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        // Generate new access token only
        String newAccessToken = jwtService.generateToken(userDetails);

        log.debug("Token refreshed for user: {}", email);
        return buildAuthResponse(user, newAccessToken, refreshToken);
    }

    // ── PRIVATE HELPERS ───────────────────────────────
    private AuthResponse buildAuthResponse(
            UserEntity user,
            String accessToken,
            String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .role(user.getRole().name())
                .build();
    }
}