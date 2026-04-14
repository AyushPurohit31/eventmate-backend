package com.eventmate.auth.dto;

import com.eventmate.auth.model.Role;

import java.util.UUID;

public class UserResponse {

    private UUID userId;
    private String email;
    private Role role;
    private UUID tenantId;
    private String firstName;
    private String lastName;

    // Constructors
    public UserResponse() {
    }

    public UserResponse(UUID userId, String email, Role role, UUID tenantId, String firstName, String lastName) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.tenantId = tenantId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
