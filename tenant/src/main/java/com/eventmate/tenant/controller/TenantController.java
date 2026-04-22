package com.eventmate.tenant.controller;
import com.eventmate.tenant.dto.RegisterTenantRequest;
import com.eventmate.tenant.dto.RegisterTenantResponse;
import com.eventmate.tenant.model.Tenant;
import com.eventmate.tenant.service.TenantService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenant")
public class TenantController {

    private static final Logger log = LoggerFactory.getLogger(TenantController.class);
    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterTenantResponse> registerTenant(@Valid @RequestBody RegisterTenantRequest request) {
        log.info("Received tenant registration request for email: {}", request.getEmail());
        RegisterTenantResponse response = tenantService.registerTenant(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Tenant>> getAllTenants() {
        log.info("Fetching all tenants");
        List<Tenant> tenants = tenantService.getAllTenants();
        return new ResponseEntity<>(tenants, HttpStatus.OK);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Tenant> findTenantBySlug(@PathVariable String slug) {
        log.info("Fetching tenant by slug: {}", slug);
        Tenant tenant = tenantService.findTenantBySlug(slug);
        return new ResponseEntity<>(tenant, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> findTenantById(@PathVariable UUID id) {
        log.info("Fetching tenant by id: {}", id);
        Tenant tenant = tenantService.findTenantById(id);
        return new ResponseEntity<>(tenant, HttpStatus.OK);
    }
}
