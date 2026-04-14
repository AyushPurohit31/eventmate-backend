package com.eventmate.booking.repository;

import com.eventmate.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    Optional<Booking> findBySeatReservationId(UUID seatReservationId);
}
