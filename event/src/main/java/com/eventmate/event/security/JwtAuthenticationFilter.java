package com.eventmate.event.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String ROLE_HEADER = "X-User-Role";

    @Value("${eventmate.internal.token:AWFAWvsdcascawEFAWEEFDASDwadwdsafaewrrfewf}")
    private String expectedInternalToken;

    @Value("${eventmate.security.enabled:true}")
    private boolean securityEnabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!securityEnabled) {
            filterChain.doFilter(request, response);
            return;
        }
        log.info("Incoming headers: X-Internal-Token={}, X-User-Id={}, X-User-Role={}",
                request.getHeader("X-Internal-Token"),
                request.getHeader("X-User-Id"),
                request.getHeader("X-User-Role"));

        String internalToken = request.getHeader(INTERNAL_TOKEN_HEADER);
        if (expectedInternalToken == null || !expectedInternalToken.equals(internalToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            log.warn("Unauthorized request: missing or invalid internal token");
            return;
        }

        String userId = request.getHeader(USER_ID_HEADER);
        String role = request.getHeader(ROLE_HEADER);

        if (userId != null && role != null) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Auth set: principal={}, authorities={}, isAuthenticated={}",
                    authentication.getName(),
                    authentication.getAuthorities(),
                    authentication.isAuthenticated());
            log.debug("Gateway-trusted auth set for userId: {} with role: {}", userId, role);
        }

        log.info("Auth in context just before chain: {}",
                SecurityContextHolder.getContext().getAuthentication());
        filterChain.doFilter(request, response);
    }
}
