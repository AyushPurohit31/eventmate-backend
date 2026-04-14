package com.eventmate.auth.controller;

import com.eventmate.auth.dto.*;
import com.eventmate.auth.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        log.info("POST /auth/register - Email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        log.info("POST /auth/login - Email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        log.info("POST /auth/refresh");
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> logout(@RequestHeader("Authorization") String token) {
        log.info("POST /auth/logout");
        authService.logout(token);
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser() {
        log.info("GET /auth/me");
        UserResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    @PreAuthorize("hasRole('SERVICE')")
    public ResponseEntity<MessageResponse> validateToken(@RequestHeader("Authorization") String token) {
        log.info("GET /auth/validate");
        boolean isValid = authService.validateToken(token);
        if (isValid) {
            return ResponseEntity.ok(new MessageResponse("Token is valid"));
        } else {
            return ResponseEntity.status(401).body(new MessageResponse("Token is invalid"));
        }
    }

    @PostMapping("/update-tenant/{userId}")
    public ResponseEntity<UserResponse> updateTenantId(@PathVariable UUID userId, @RequestBody UpdateTenantRequest request){
        UUID tenantId = request.getTenantId();
        log.info("Updating tenant ID: {} for user with ID: {}", tenantId, userId);
        UserResponse response = authService.updateTenantId(userId, tenantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        log.info("Fetching user details for userId: {}", userId);
        UserResponse response = authService.getUserById(userId);
        return ResponseEntity.ok(response);
    }
}
