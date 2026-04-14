package com.eventmate.tenant.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class AuthClient {

    private static final String authServiceUrl = "http://localhost:8081/auth/update-tenant/";
    private final RestTemplate restTemplate;

    public AuthClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void addTenantToUser(UUID userId, UUID tenantId){
        String url = authServiceUrl + userId;
        UpdateTenantRequest request = new UpdateTenantRequest(tenantId);

        HttpHeaders headers = new HttpHeaders();

        HttpEntity<UpdateTenantRequest> requestEntity = new HttpEntity<>(request, headers);
        restTemplate.postForObject(url, requestEntity, Void.class);
    }
}
