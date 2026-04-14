package com.eventmate.auth.service;

import com.eventmate.auth.model.RefreshToken;
import com.eventmate.auth.repository.RefreshTokenRepository;
import com.eventmate.auth.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    public RefreshToken createRefreshToken(UUID userId) {
        String token = jwtUtil.generateRefreshToken(userId);
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000);

        RefreshToken refreshToken = new RefreshToken(userId, token, expiryDate);
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token has expired. Please login again.");
        }
        return token;
    }

    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }

    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public RefreshToken rotateRefreshToken(String oldToken) {
        RefreshToken oldRefreshToken = findByToken(oldToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        verifyExpiration(oldRefreshToken);

        if (oldRefreshToken.getRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        // Revoke old token
        oldRefreshToken.setRevoked(true);
        refreshTokenRepository.save(oldRefreshToken);

        // Create new token
        return createRefreshToken(oldRefreshToken.getUserId());
    }
}

