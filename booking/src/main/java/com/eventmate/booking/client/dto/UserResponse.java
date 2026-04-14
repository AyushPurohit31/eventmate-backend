package com.eventmate.booking.client.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserResponse {
    private UUID userId;
    private String email;
    private String role;
    private UUID tenantId;
    private String firstName;
    private String lastName;
}
