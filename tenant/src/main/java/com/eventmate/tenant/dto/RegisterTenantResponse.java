package com.eventmate.tenant.dto;

import com.eventmate.tenant.model.Subscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterTenantResponse {

    private UUID id;
    private String name;
    private String email;
    private String slug;
    private Subscription subscription;
    private Integer maxUsers;
    private Integer maxEvents;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
