package com.eventmate.event.service;

import com.eventmate.event.dto.RegisterEventRequest;
import com.eventmate.event.dto.EventResponse;
import com.eventmate.event.dto.UpdateEventRequest;
import com.eventmate.event.exception.EventNotFoundException;
import com.eventmate.event.model.Event;
import com.eventmate.event.model.EventStatus;
import com.eventmate.event.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventManagement {

    private final EventRepository eventRepository;
    private static final Logger log  = LoggerFactory.getLogger(EventManagement.class);

    public EventManagement(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<EventResponse> getAllEventsResponse() {
        return getAllEvents().stream()
                .map(this::toEventResponse)
                .collect(Collectors.toList());
    }

    public List<Event> getEventsByTenantId(UUID tenantId) {
        return eventRepository.findAllByTenantId(tenantId);
    }

    public List<EventResponse> getEventsByTenantIdResponse(UUID tenantId) {
        return getEventsByTenantId(tenantId).stream()
                .map(this::toEventResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventResponse registerEvent(UUID tenantId, RegisterEventRequest request) {
        log.info("Registering new event with name: {}", request.getTitle());
        Event event  = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventType(request.getEventCategory());
        event.setTenantId(tenantId);
        event.setImageUrl(request.getImageUrl());
        event.setBannerImageUrl(request.getBannerImageUrl());
        event.setStatus(EventStatus.SCHEDULED);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());

        Event savedEvent = eventRepository.save(event);
        log.info("Event registered successfully with id: {}", savedEvent.getId());

        return toEventResponse(savedEvent);
    }

    @Transactional
    public EventResponse updateEvent(UUID tenantId, UUID eventId, UpdateEventRequest request) {
        Optional<Event> eventOpt = eventRepository.findByIdAndTenantId(eventId, tenantId);
        if (eventOpt.isEmpty()) {
            log.warn("Event with id: {} not found for tenantId: {}", eventId, tenantId);
            throw new EventNotFoundException("Event not found");
        }
        Event event = eventOpt.get();
        // Update fields from request
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventType(request.getEventCategory());
        event.setImageUrl(request.getImageUrl());
        event.setBannerImageUrl(request.getBannerImageUrl());
        event.setUpdatedAt(LocalDateTime.now());
        Event updatedEvent = eventRepository.save(event);
        return toEventResponse(updatedEvent);
    }

    @Transactional
    public EventResponse deleteEvent(UUID eventId) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            log.warn("Event with id: {} not found for deletion", eventId);
            throw new EventNotFoundException("Event not found");
        }
        Event event = eventOpt.get();
        event.setStatus(EventStatus.CANCELLED);
        event.setUpdatedAt(LocalDateTime.now());
        Event cancelledEvent = eventRepository.save(event);
        return toEventResponse(cancelledEvent);
    }

    // Centralized mapper: entity -> DTO (without schedules to avoid recursion)
    private EventResponse toEventResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .tenantId(event.getTenantId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventType(event.getEventType())
                .imageUrl(event.getImageUrl())
                .bannerImageUrl(event.getBannerImageUrl())
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
