package com.eventmate.event.repository;

import com.eventmate.event.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findAllByTenantId(UUID tenantId);
    Optional<Event> findByIdAndTenantId(UUID eventId, UUID tenantId);
}
