package com.eventmate.event.service;

import com.eventmate.event.dto.RegisteredSeatResponse;
import com.eventmate.event.dto.SeatRegistrationRequest;
import com.eventmate.event.dto.SeatRegistrationResponse;
import com.eventmate.event.exception.SeatConflictException;
import com.eventmate.event.model.EventSchedule;
import com.eventmate.event.model.SeatInventory;
import com.eventmate.event.model.SeatStatus;
import com.eventmate.event.repository.SeatInventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SeatReservationUtil {

    private final Logger log = LoggerFactory.getLogger(SeatReservationUtil.class);
    private final SeatInventoryRepository seatInventoryRepository;

    public SeatReservationUtil(SeatInventoryRepository seatInventoryRepository) {
        this.seatInventoryRepository = seatInventoryRepository;
    }

    @Transactional
    public SeatRegistrationResponse reserveSeatInventory(UUID scheduleId,
                                                         SeatRegistrationRequest request,
                                                         LocalDateTime expiresAt) {
        UUID userId = request.getUserId();
        List<UUID> seatIds = request.getSeatInventoryIds();
        UUID seatReservationId = UUID.randomUUID();

        int updated = seatInventoryRepository.reserveSeats(
                seatIds,
                SeatStatus.RESERVED,
                userId,
                expiresAt,
                seatReservationId
        );

        if (updated != seatIds.size()) {
            throw new SeatConflictException("Some seats already booked/held");
        }

        List<SeatInventory> seats = seatInventoryRepository.findAllById(seatIds);

        EventSchedule schedule = seats.getFirst().getSchedule();

        List<RegisteredSeatResponse> seatDetails = seats.stream()
                .map(seat -> RegisteredSeatResponse.builder()
                        .seatInventoryId(seat.getId())
                        .seatRow(seat.getVenueSeat().getRowNumber())
                        .seatNumber(seat.getVenueSeat().getSeatNumber())
                        .status(seat.getStatus())
                        .price(seat.getPrice())
                        .build())
                .toList();

        BigDecimal totalAmount = seats.stream()
                .map(SeatInventory::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Reserved {} seats for scheduleId: {} with reservationId: {}",
                seatIds.size(), scheduleId, seatReservationId);

        return SeatRegistrationResponse.builder()
                .status("SUCCESS")
                .price(totalAmount)
                .tenantId(schedule.getEvent().getTenantId())
                .eventName(schedule.getEvent().getTitle())
                .venueName(schedule.getVenue().getName())
                .eventStartDateTime(schedule.getStartDateTime())
                .seatDetails(seatDetails)
                .seatReservationId(seatReservationId)
                .seatExpirationTime(expiresAt)
                .build();
    }
}
