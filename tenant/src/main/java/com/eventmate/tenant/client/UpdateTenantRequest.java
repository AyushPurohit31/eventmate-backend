package com.eventmate.tenant.client;

import java.util.UUID;

public class UpdateTenantRequest {
    private UUID tenantId;

    public UpdateTenantRequest() {}
    public UpdateTenantRequest(UUID tenantId) {
        this.tenantId = tenantId;
    }
    public UUID getTenantId() {
        return tenantId;
    }
    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }
}

