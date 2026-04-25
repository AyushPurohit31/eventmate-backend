package com.eventmate.booking.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    private static final Logger log = LoggerFactory.getLogger(FeignConfig.class);

    public FeignConfig() {
        System.out.println(">>> FeignConfig LOADED <<<");
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        log.info("Creating Feign RequestInterceptor bean to propagate headers");
        return requestTemplate -> {

            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attrs == null) return;

            HttpServletRequest request = attrs.getRequest();

            String internalToken = request.getHeader("X-Internal-Token");
            String userId = request.getHeader("X-User-Id");
            String role = request.getHeader("X-User-Role");

            if (internalToken != null) {
                requestTemplate.header("X-Internal-Token", internalToken);
            }
            if (userId != null) {
                requestTemplate.header("X-User-Id", userId);
            }
            if (role != null) {
                requestTemplate.header("X-User-Role", role);
            }
        };
    }
}
