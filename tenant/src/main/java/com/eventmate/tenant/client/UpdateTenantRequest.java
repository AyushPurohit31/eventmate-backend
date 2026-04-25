package com.eventmate.tenant.client;

import lombok.Getter;

import java.util.UUID;

@Getter
public class UpdateTenantRequest {
    private UUID tenantId;

    public UpdateTenantRequest() {}
    public UpdateTenantRequest(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }
}

