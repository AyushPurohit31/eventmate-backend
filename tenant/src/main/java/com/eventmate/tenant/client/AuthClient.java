package com.eventmate.tenant.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class AuthClient {

    private static final String authServiceUrl = "http://auth/auth/update-tenant/";
    private final RestTemplate restTemplate;

    public AuthClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void addTenantToUser(UUID userId, UUID tenantId){
        String url = authServiceUrl + userId;
        UpdateTenantRequest request = new UpdateTenantRequest(tenantId);

        // Send DTO directly; no need to wrap with HttpEntity/headers.
        restTemplate.postForObject(url, request, Void.class);
    }
}
