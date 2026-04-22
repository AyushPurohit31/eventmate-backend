package com.eventmate.booking.client;

import com.eventmate.booking.client.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "auth-service", path = "/api/auth")
public interface UserClient {

    @GetMapping("/user/{userId}")
    UserResponse getUser(@PathVariable("userId") UUID userId);
}
