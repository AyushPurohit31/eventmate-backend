package com.eventmate.auth.service;

import com.eventmate.auth.dto.*;
import com.eventmate.auth.exception.DuplicateEmailException;
import com.eventmate.auth.exception.UserNotFoundException;
import com.eventmate.auth.model.RefreshToken;
import com.eventmate.auth.model.Role;
import com.eventmate.auth.model.User;
import com.eventmate.auth.repository.UserRepository;
import com.eventmate.auth.security.JwtUtil;
import com.eventmate.auth.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email is already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER); // Set to USER by default
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setTenantId(request.getTenantId());
        user.setIsActive(true);

        user = userRepository.save(user);
        log.info("User registered successfully with ID: {} and role: USER", user.getId());

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getTenantId()
        );

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt with email: {}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(
                userPrincipal.getId(),
                userPrincipal.getFirstName(),
                userPrincipal.getLastName(),
                userPrincipal.getEmail(),
                userPrincipal.getRole(),
                userPrincipal.getTenantId()
        );

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId());

        log.info("User logged in successfully: {}", request.getEmail());
        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refresh token request");

        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(request.getRefreshToken());

        User user = userRepository.findById(newRefreshToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getTenantId()
        );

        log.info("Tokens refreshed successfully for user: {}", user.getEmail());
        return new AuthResponse(accessToken, newRefreshToken.getToken());
    }

    @Transactional
    public void logout(String token) {
        log.info("User logout request");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        UUID userId = UUID.fromString(jwtUtil.extractUserId(token));
        refreshTokenService.revokeAllUserTokens(userId);

        log.info("User logged out successfully");
    }

    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        return new UserResponse(
                userPrincipal.getId(),
                userPrincipal.getEmail(),
                userPrincipal.getRole(),
                userPrincipal.getTenantId(),
                userPrincipal.getFirstName(),
                userPrincipal.getLastName()
        );
    }

    public boolean validateToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.validateToken(token);
    }

    @Transactional
    public UserResponse updateTenantId(UUID userId, UUID tenantId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        User u = user.get();
        u.setTenantId(tenantId);
        Role role = u.getRole();
        if(role == Role.USER){
            u.setRole(Role.TENANT);
        }
        userRepository.save(u);
        log.info("Updated tenant ID: {} for user with ID: {}", tenantId, user);
        return new UserResponse(
                u.getId(),
                u.getEmail(),
                u.getRole(),
                u.getTenantId(),
                u.getFirstName(),
                u.getLastName()
        );
    }

    public UserResponse getUserById(UUID userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        User u = user.get();
        return new UserResponse(
                u.getId(),
                u.getEmail(),
                u.getRole(),
                u.getTenantId(),
                u.getFirstName(),
                u.getLastName()
        );
    }
}

