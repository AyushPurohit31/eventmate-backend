package com.eventmate.payment.config;

import com.eventmate.payment.security.JwtAuthenticationFilter;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.context.SecurityContextHolderFilter;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${eventmate.security.enabled:true}")
    private boolean securityEnabled;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        if (!securityEnabled) {
            return http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).build();
        }
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/payments/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterAfter(jwtAuthenticationFilter, SecurityContextHolderFilter.class);
        return http.build();
    }

    @PostConstruct
    public void init() {
        log.info(">>> SecurityConfig LOADED <<<");
    }

    @Bean
    public ApplicationRunner logSecurityFlagOnStartup(Environment environment) {
        return args -> log.info("Startup config: activeProfiles={}, eventmate.security.enabled={}",
                Arrays.toString(environment.getActiveProfiles()), securityEnabled);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration() {
        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(jwtAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
    }
}
