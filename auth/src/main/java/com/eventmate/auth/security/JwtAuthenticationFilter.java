package com.eventmate.auth.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    // Internal headers
    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String ROLE_HEADER = "X-User-Role";

    @Value("${eventmate.internal.token}")
    private String expectedInternalToken;

    // JWT header
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String internalToken = request.getHeader(INTERNAL_TOKEN_HEADER);
            if (expectedInternalToken != null && expectedInternalToken.equals(internalToken)) {
                String userId = request.getHeader(USER_ID_HEADER);
                String role = request.getHeader(ROLE_HEADER);

                if (userId != null && role != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Internal auth set for userId: {} with role: {}", userId, role);

                    filterChain.doFilter(request, response);
                    return;
                }
            }

            // EXTERNAL CALL (JWT)
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);

            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {

                String token = authHeader.substring(7);

                if (jwtUtil.validateToken(token)) {
                    Claims claims = jwtUtil.extractAllClaims(token);

                    String userId = claims.getSubject();
                    String role = claims.get("role", String.class);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("JWT auth set for userId: {} with role: {}", userId, role);
                }
            }

        } catch (Exception ex) {
            log.error("Authentication failed", ex);
        }

        filterChain.doFilter(request, response);
    }
}