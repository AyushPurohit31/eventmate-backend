package com.eventmate.event.controller;

import com.eventmate.event.dto.*;
import com.eventmate.event.service.EventManagement;
import com.eventmate.event.service.EventScheduleManagement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/events")
public class EventController {

    private final EventManagement eventManagement;
    private final EventScheduleManagement eventScheduleManagement;
    private final Logger log = LoggerFactory.getLogger(EventController.class);

    public EventController(EventManagement eventManagement, EventScheduleManagement eventScheduleManagement) {
        this.eventManagement = eventManagement;
        this.eventScheduleManagement = eventScheduleManagement;
    }

    @GetMapping("/event")
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        log.info("Fetching all the events");
        List<EventResponse> events = eventManagement.getAllEventsResponse();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @PostMapping("/tenant/register/{tenantId}")
    public ResponseEntity<EventResponse> registerEvent(@PathVariable UUID tenantId, @Valid @RequestBody RegisterEventRequest request) {
        log.info("Received event registration request for title: {}", request.getTitle());
        EventResponse response = eventManagement.registerEvent(tenantId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/event/{tenantId}")
    public ResponseEntity<List<EventResponse>> getEventsByTenantId(@PathVariable UUID tenantId) {
        log.info("Fetching events for tenantId: {}", tenantId);
        List<EventResponse> events = eventManagement.getEventsByTenantIdResponse(tenantId);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @PatchMapping("/tenant/{tenantId}/event/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable UUID tenantId, @PathVariable UUID eventId, @RequestBody UpdateEventRequest request) {
        log.info("Updating event for eventId: {}", eventId);
        EventResponse response = eventManagement.updateEvent(tenantId, eventId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/tenant/{eventId}")
    public ResponseEntity<EventResponse> deleteEvent(@PathVariable UUID eventId) {
        log.info("Deleting event for eventId: {}", eventId);
        EventResponse response = eventManagement.deleteEvent(eventId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/tenant/schedule/register")
    public ResponseEntity<EventScheduleResponse> registerEventSchedule(@Valid @RequestBody RegisterEventScheduleRequest request) {
        log.info("Registering new event schedule for eventId: {} and venueId: {}", request.getEventId(), request.getVenueId());
        EventScheduleResponse response = eventScheduleManagement.registerEventSchedule(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/tenant/schedule/{scheduleId}")
    public ResponseEntity<EventScheduleResponse> updateEventSchedule(@PathVariable UUID scheduleId, @RequestBody UpdateEventScheduleRequest request) {
        log.info("Updating event schedule with id: {}", scheduleId);
        EventScheduleResponse response = eventScheduleManagement.updateEventSchedule(scheduleId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/reserve-seats/{scheduleId}")
    public ResponseEntity<SeatRegistrationResponse> reserveSeats(@PathVariable UUID scheduleId, @Valid @RequestBody SeatRegistrationRequest request) {
        log.info("Reserving seats for userId: {} and seatInventoryIds: {}", request.getUserId(), request.getSeatInventoryIds());
        SeatRegistrationResponse response = eventScheduleManagement.reserveSeat(scheduleId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/release-seats/{userId}")
    public ResponseEntity<Void> releaseSeats(@PathVariable UUID userId, @Valid @RequestBody List<UUID> seatInventoryIds) {
        eventScheduleManagement.releaseSeats(userId, seatInventoryIds);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<EventScheduleResponse> getEventScheduleById(@PathVariable UUID scheduleId) {
        log.info("Fetching event schedule for scheduleId: {}", scheduleId);
        EventScheduleResponse response = eventScheduleManagement.getEventScheduleById(scheduleId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/reserve-seat/{seatReservationId}")
    public ResponseEntity<SeatHoldDetailsResponse> getSeatHoldDetails(@PathVariable UUID seatReservationId) {
        log.info("Fetching seat hold details for seatReservationId: {}", seatReservationId);
        SeatHoldDetailsResponse response = eventScheduleManagement.getSeatHoldDetails(seatReservationId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
