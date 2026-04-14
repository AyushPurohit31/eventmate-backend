package com.eventmate.tenant.service;

import com.eventmate.tenant.client.AuthClient;
import com.eventmate.tenant.dto.RegisterTenantRequest;
import com.eventmate.tenant.dto.RegisterTenantResponse;
import com.eventmate.tenant.exception.DuplicateEmailException;
import com.eventmate.tenant.exception.DuplicateSlugException;
import com.eventmate.tenant.exception.TenantNotFoundException;
import com.eventmate.tenant.model.Subscription;
import com.eventmate.tenant.model.Tenant;
import com.eventmate.tenant.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TenantService {

    private static final Logger log = LoggerFactory.getLogger(TenantService.class);
    private final TenantRepository tenantRepository;
    private final AuthClient authClient;

    public TenantService(TenantRepository tenantRepository, AuthClient authClient) {
        this.tenantRepository = tenantRepository;
        this.authClient = authClient;
    }

    @Transactional
    public RegisterTenantResponse registerTenant(RegisterTenantRequest request) {
        log.info("Attempting to register tenant with email: {}", request.getEmail());

        if (tenantRepository.existsByEmail(request.getEmail())) {
            log.warn("Tenant registration failed: Email {} already exists", request.getEmail());
            throw new DuplicateEmailException("Email already exists");
        }

        if (tenantRepository.existsBySlug(request.getSlug())) {
            log.warn("Tenant registration failed: Slug {} already exists", request.getSlug());
            throw new DuplicateSlugException("Slug already exists");
        }

        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        tenant.setEmail(request.getEmail());
        tenant.setSlug(request.getSlug());
        tenant.setSubscription(Subscription.BASIC);
        tenant.setSchemaName("tenant_"+request.getSlug());
        tenant.setMaxEvents(10);
        tenant.setIsActive(true);
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setUpdatedAt(LocalDateTime.now());

        Tenant savedTenant = tenantRepository.save(tenant);

        if (request.getUserId() != null) {
            authClient.addTenantToUser(request.getUserId(), savedTenant.getId());
        }

        log.info("Tenant registered successfully with ID: {}", savedTenant.getId());

        return RegisterTenantResponse.builder()
                .id(savedTenant.getId())
                .name(savedTenant.getName())
                .email(savedTenant.getEmail())
                .slug(savedTenant.getSlug())
                .subscription(savedTenant.getSubscription())
                .maxEvents(savedTenant.getMaxEvents())
                .isActive(savedTenant.getIsActive())
                .createdAt(savedTenant.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<Tenant> getAllTenants() {
        log.info("Fetching all tenants");
        List<Tenant> tenants = tenantRepository.findAll();
        log.info("Retrieved {} tenants", tenants.size());
        return tenants;
    }

    @Transactional(readOnly = true)
    public Tenant findTenantBySlug(String slug){
        Optional<Tenant> tenant = tenantRepository.findBySlug(slug);
        if(tenant.isEmpty()){
            log.warn("Tenant with slug {} not found", slug);
            throw new TenantNotFoundException("Tenant not found");
        }
        log.info("Tenant with slug {} found", slug);
        return tenant.get();
    }

    @Transactional(readOnly = true)
    public Tenant findTenantById(UUID id){
        Optional<Tenant> tenant = tenantRepository.findById(id);
        if(tenant.isEmpty()){
            log.warn("Tenant with id {} not found", id);
            throw new TenantNotFoundException("Tenant with id not found");
        }
        log.info("Tenant with id {} found", id);
        return tenant.get();
    }
}

