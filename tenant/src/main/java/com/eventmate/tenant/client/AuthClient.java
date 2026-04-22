package com.eventmate.tenant.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "auth-service", path = "/api/auth")
public interface AuthClient {

    @PostMapping("/update-tenant/{userId}")
    void addTenantToUser(@PathVariable("userId") UUID userId, @RequestBody UpdateTenantRequest request);
}
