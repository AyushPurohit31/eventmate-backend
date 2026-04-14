package com.eventmate.event.service;

import com.eventmate.event.dto.BookingEvent;
import com.eventmate.event.model.SeatInventory;
import com.eventmate.event.model.SeatStatus;
import com.eventmate.event.repository.SeatInventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class BookingEventListener {

    private static final Logger log = LoggerFactory.getLogger(BookingEventListener.class);

    private final SeatInventoryRepository seatInventoryRepository;
    private final EventScheduleManagement eventScheduleManagement;
    private final SeatLockService seatLockService;

    public BookingEventListener(SeatInventoryRepository seatInventoryRepository,
                                EventScheduleManagement eventScheduleManagement,
                                SeatLockService seatLockService) {
        this.seatInventoryRepository = seatInventoryRepository;
        this.eventScheduleManagement = eventScheduleManagement;
        this.seatLockService = seatLockService;
    }

    @KafkaListener(topics = "booking-events", groupId = "event-service")
    @Transactional
    public void handleBookingEvent(BookingEvent event) {
        if (event == null) {
            log.warn("Received null BookingEvent. Skipping.");
            return;
        }

        UUID bookingId = event.getBookingId();
        UUID seatReservationId = event.getSeatReservationId();
        UUID userId = event.getUserId();
        String eventType = event.getEventType();

        log.info("Received booking event: bookingId={}, seatReservationId={}, userId={}, eventType={}",
                bookingId, seatReservationId, userId, eventType);

        if (seatReservationId == null || userId == null) {
            log.warn("Invalid BookingEvent missing seatReservationId or userId. Skipping.");
            return;
        }

        List<SeatInventory> seats = seatInventoryRepository
                .findBySeatReservationIdAndStatus(seatReservationId, SeatStatus.RESERVED);

        if (seats == null || seats.isEmpty()) {
            log.info("No RESERVED seats found for seatReservationId {}. Event likely already processed or expired.",
                    seatReservationId);
            return;
        }

        if(Objects.equals(eventType, "booking-confirmed")){
            try {
                eventScheduleManagement.getSeatHoldDetails(seatReservationId);
            } catch (Exception e) {
                log.error("Seat hold details inconsistent for seatReservationId {}. Skipping event.", seatReservationId, e);
                return;
            }
        }

        SeatStatus targetStatus;
        if ("booking-confirmed".equalsIgnoreCase(eventType)) {
            targetStatus = SeatStatus.BOOKED;
        } else if ("booking-failed".equalsIgnoreCase(eventType)) {
            targetStatus = SeatStatus.AVAILABLE;
        } else {
            log.warn("Unknown booking eventType '{}' for seatReservationId {}. Skipping.", eventType, seatReservationId);
            return;
        }

        boolean allAlreadyTarget = seats.stream().allMatch(seat -> seat.getStatus() == targetStatus);
        if (allAlreadyTarget) {
            log.info("All seats for reservation {} already in status {}. Skipping update.",
                    seatReservationId, targetStatus);
            return;
        }

        for (SeatInventory seat : seats) {
            if (seat.getStatus() != SeatStatus.RESERVED) {
                log.warn("Seat {} for reservation {} is in status {} not RESERVED; skipping.",
                        seat.getId(), seatReservationId, seat.getStatus());
                continue;
            }
            seat.setStatus(targetStatus);
            if(targetStatus == SeatStatus.AVAILABLE) {
                seat.setReservationExpiryTime(null);
                seat.setReservedByUserId(null);
                seat.setSeatReservationId(null);
            }
        }

        seatInventoryRepository.saveAll(seats);
        log.info("Updated {} seats for reservation {} to status {} based on booking event {}.",
                seats.size(), seatReservationId, targetStatus, eventType);

        try {
            seatLockService.releaseSeats(userId,
                    seats.stream().map(SeatInventory::getId).toList());
            log.info("Released seat locks in Redis for userId {} and reservation {}.", userId, seatReservationId);
        } catch (Exception e) {
            log.error("Failed to release seat locks in Redis for userId {} and reservation {}.",
                    userId, seatReservationId, e);
        }
    }
}
