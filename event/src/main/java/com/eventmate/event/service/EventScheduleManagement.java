package com.eventmate.event.service;

import com.eventmate.event.dto.*;
import com.eventmate.event.exception.EventNotFoundException;
import com.eventmate.event.exception.SeatConflictException;
import com.eventmate.event.exception.SeatReservationException;
import com.eventmate.event.exception.VenueNotFoundException;
import com.eventmate.event.exception.ResourceNotFoundException;
import com.eventmate.event.model.*;
import com.eventmate.event.repository.EventRepository;
import com.eventmate.event.repository.EventScheduleRepository;
import com.eventmate.event.repository.SeatInventoryRepository;
import com.eventmate.event.repository.VenueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EventScheduleManagement {

    private final EventScheduleRepository eventScheduleRepository;
    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final SeatInventoryRepository seatInventoryRepository;
    private final SeatLockService seatLockService;
    private final SeatReservationUtil seatReservationUtil;
    private final Logger log = LoggerFactory.getLogger(EventScheduleManagement.class);
    private static final int SEAT_HOLD_EXPIRATION_MINUTES = 10;

    public EventScheduleManagement(EventScheduleRepository eventScheduleRepository,
                                   EventRepository eventRepository,
                                   VenueRepository venueRepository,
                                   SeatInventoryRepository seatInventoryRepository,
                                   SeatLockService seatLockService,
                                   SeatReservationUtil seatReservationUtil) {
        this.eventScheduleRepository = eventScheduleRepository;
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.seatInventoryRepository = seatInventoryRepository;
        this.seatLockService = seatLockService;
        this.seatReservationUtil = seatReservationUtil;
    }

    public EventScheduleResponse getEventScheduleById(UUID scheduleId) {
        EventSchedule schedule = eventScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EventNotFoundException("Event schedule not found: " + scheduleId));
        log.info("Fetched event schedule with id: {}", scheduleId);
        return buildResponse(schedule, schedule.getEvent(), schedule.getVenue());
    }

    @Transactional
    public EventScheduleResponse registerEventSchedule(RegisterEventScheduleRequest request) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new EventNotFoundException("Event not found: " + request.getEventId()));

        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new VenueNotFoundException("Venue not found: " + request.getVenueId()));

        EventSchedule schedule = new EventSchedule();
        schedule.setEvent(event);
        schedule.setVenue(venue);
        schedule.setStartDateTime(request.getStartDateTime());
        schedule.setEndDateTime(request.getEndDateTime());
        schedule.setAvailablePasses(request.getAvailablePasses());
        schedule.setCreatedAt(LocalDateTime.now());

        EventSchedule savedSchedule = eventScheduleRepository.save(schedule);

        List<VenueSeat> venueSeats = venue.getSeats();
        if (venueSeats.isEmpty()) {
            throw new IllegalStateException("Venue has no seat layout defined");
        }
        List<SeatInventory> seatInventories = new ArrayList<>();
        for (VenueSeat venueSeat : venueSeats) {
            SeatInventory seatInventory = new SeatInventory();
            seatInventory.setSchedule(savedSchedule);
            seatInventory.setVenueSeat(venueSeat);
            seatInventory.setStatus(SeatStatus.AVAILABLE);
            seatInventory.setPrice(request.getTicketPrice());
            seatInventories.add(seatInventory);
        }
        seatInventoryRepository.saveAll(seatInventories);
        log.info("Event schedule registered successfully with id: {}", savedSchedule.getId());

        return buildResponse(savedSchedule, event, venue);
    }

    @Transactional
    public EventScheduleResponse updateEventSchedule(UUID scheduleId, UpdateEventScheduleRequest request) {
        EventSchedule schedule = eventScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EventNotFoundException("Event schedule not found: " + scheduleId));

        if (request.getVenueId() != null && !request.getVenueId().equals(schedule.getVenue().getId())) {
            Venue newVenue = venueRepository.findById(request.getVenueId())
                    .orElseThrow(() -> new VenueNotFoundException("Venue not found: " + request.getVenueId()));

            // Delete old seat inventory for this schedule
            seatInventoryRepository.deleteAllByScheduleId(schedule.getId());
            log.info("Deleted old seat inventory for schedule: {}", schedule.getId());

            schedule.setVenue(newVenue);

            // Regenerate seat inventory from new venue's seat layout
            List<VenueSeat> newVenueSeats = newVenue.getSeats();
            if (newVenueSeats == null || newVenueSeats.isEmpty()) {
                throw new IllegalStateException("New venue has no seat layout defined");
            }
            List<SeatInventory> seatInventories = new ArrayList<>();
            for (VenueSeat venueSeat : newVenueSeats) {
                SeatInventory seatInventory = new SeatInventory();
                seatInventory.setSchedule(schedule);
                seatInventory.setVenueSeat(venueSeat);
                seatInventory.setStatus(SeatStatus.AVAILABLE);
                seatInventories.add(seatInventory);
            }
            seatInventoryRepository.saveAll(seatInventories);
            log.info("Regenerated seat inventory for schedule: {} with new venue: {}", schedule.getId(), newVenue.getId());
        }
        if (request.getStartDateTime() != null) {
            schedule.setStartDateTime(request.getStartDateTime());
        }
        if (request.getEndDateTime() != null) {
            schedule.setEndDateTime(request.getEndDateTime());
        }
        if (request.getAvailablePasses() != null) {
            schedule.setAvailablePasses(request.getAvailablePasses());
        }

        EventSchedule updated = eventScheduleRepository.save(schedule);
        log.info("Event schedule updated successfully with id: {}", updated.getId());

        return buildResponse(updated, updated.getEvent(), updated.getVenue());
    }

    public SeatRegistrationResponse reserveSeat(UUID scheduleId, SeatRegistrationRequest request) {
        List<UUID> seatIds = request.getSeatInventoryIds();
        UUID userId = request.getUserId();

        if (seatIds == null || seatIds.isEmpty()) {
            throw new SeatReservationException("No seats selected");
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(SEAT_HOLD_EXPIRATION_MINUTES);
        LockResult lockResult = seatLockService.lockSeats(userId, seatIds, expiresAt);

        try {
            return seatReservationUtil.reserveSeatInventory(scheduleId, request, expiresAt);
        } catch (Exception e) {
            if (!lockResult.getNewlyLockedSeatIds().isEmpty()) {
                seatLockService.releaseSeats(userId, lockResult.getNewlyLockedSeatIds());
            }
            throw new SeatReservationException(e.getMessage(), e);
        }
    }

    public void releaseSeats(UUID userId, List<UUID> seatInventoryIds) {
        try {
            int updated = seatInventoryRepository.releaseSeats(seatInventoryIds, SeatStatus.AVAILABLE, userId);
            if (updated != seatInventoryIds.size()) {
                throw new SeatConflictException("Could not release all seats. Some seats may have been reserved by others or already released.");
            }
            log.info("Released seat inventory for seatInventoryIds: {} by userId: {}", seatInventoryIds, userId);
            seatLockService.releaseSeats(userId, seatInventoryIds);
        } catch (Exception e){
            throw new SeatReservationException(e.getMessage());
        }
    }

    public SeatHoldDetailsResponse getSeatHoldDetails(UUID seatReservationId) {
        log.info("Fetching seat hold details for seatReservationId: {}", seatReservationId);
        List<SeatInventory> seats = seatInventoryRepository
                .findBySeatReservationIdAndStatus(seatReservationId, SeatStatus.RESERVED);

        if (seats == null || seats.isEmpty()) {
            throw new ResourceNotFoundException("Seat hold not found");
        }

        UUID userId = seats.getFirst().getReservedByUserId();
        UUID scheduleId = seats.getFirst().getSchedule().getId();
        LocalDateTime expiresAt = seats.getFirst().getReservationExpiryTime();

        for (SeatInventory seat : seats) {
            if (!seatReservationId.equals(seat.getSeatReservationId())) {
                throw new IllegalStateException("Inconsistent seatReservationId in DB");
            }

            if (!userId.equals(seat.getReservedByUserId())) {
                throw new IllegalStateException("Seats belong to different users");
            }

            if (!scheduleId.equals(seat.getSchedule().getId())) {
                throw new IllegalStateException("Seats belong to different schedules");
            }
        }

        if (expiresAt.isBefore(LocalDateTime.now())) {
            throw new SeatReservationException("Seat Reservation expired");
        }

        EventScheduleResponse schedule = getEventScheduleById(scheduleId);

        List<SeatDetail> seatDetails = seats.stream()
                .map(seat -> SeatDetail.builder()
                        .seatInventoryId(seat.getId())
                        .seatRow(seat.getVenueSeat().getRowNumber())
                        .seatNumber(seat.getVenueSeat().getSeatNumber())
                        .price(seat.getPrice())
                        .build())
                .toList();

        BigDecimal totalAmount = seats.stream()
                .map(SeatInventory::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SeatHoldDetailsResponse.builder()
                .seatReservationId(seatReservationId)
                .userId(userId)
                .scheduleId(scheduleId)
                .tenantId(schedule.getEvent().getTenantId())
                .eventName(schedule.getEvent().getTitle())
                .venueName(schedule.getVenue().getName())
                .eventStartDateTime(schedule.getStartDateTime())
                .expiresAt(expiresAt)
                .totalAmount(totalAmount)
                .seatDetails(seatDetails)
                .build();
    }

    private EventScheduleResponse buildResponse(EventSchedule schedule, Event event, Venue venue) {
        return EventScheduleResponse.builder()
                .id(schedule.getId())
                .startDateTime(schedule.getStartDateTime())
                .endDateTime(schedule.getEndDateTime())
                .event(EventResponse.builder()
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
                        .build())
                .venue(VenueResponse.builder()
                        .id(venue.getId())
                        .tenantId(venue.getTenantId())
                        .name(venue.getName())
                        .latitude(venue.getLatitude())
                        .longitude(venue.getLongitude())
                        .address(venue.getAddress())
                        .city(venue.getCity())
                        .state(venue.getState())
                        .country(venue.getCountry())
                        .build())
                .build();
    }
}
