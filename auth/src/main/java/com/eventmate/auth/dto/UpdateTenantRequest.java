package com.eventmate.auth.dto;

import java.util.UUID;

public class UpdateTenantRequest {
    private UUID tenantId;

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }
}

