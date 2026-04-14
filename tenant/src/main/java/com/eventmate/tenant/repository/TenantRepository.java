package com.eventmate.tenant.repository;

import com.eventmate.tenant.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    boolean existsByEmail(String email);
    boolean existsBySlug(String slug);
    Optional<Tenant> findBySlug(String slug);
}
