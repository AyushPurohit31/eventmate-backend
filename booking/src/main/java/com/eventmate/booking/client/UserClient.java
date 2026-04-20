package com.eventmate.booking.client;

import com.eventmate.booking.client.dto.UserResponse;
import com.eventmate.booking.exception.ExternalServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class UserClient {

    private final String BASE_URL = "http://auth/auth/user";
    private final RestTemplate restTemplate;

    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public UserResponse getUser(UUID userId){
        String baseUrl = BASE_URL + "/" + userId.toString();
        try {
            return restTemplate.getForObject(baseUrl, UserResponse.class);
        } catch (Exception e) {
            throw new ExternalServiceException("Failed to fetch user details for userId: " + userId, e);
        }
    }
}
