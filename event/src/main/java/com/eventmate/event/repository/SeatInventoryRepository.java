package com.eventmate.event.repository;

import com.eventmate.event.model.SeatInventory;
import com.eventmate.event.model.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SeatInventoryRepository extends JpaRepository<SeatInventory, UUID> {
    void deleteAllByScheduleId(UUID scheduleId);

    @Modifying
    @Query("""
        UPDATE SeatInventory s
        SET s.status = :status,
            s.reservedByUserId = :userId,
            s.reservationExpiryTime = :expiryTime,
            s.seatReservationId = :seatReservationId
        WHERE s.id IN :ids
          AND (
                s.status = 'AVAILABLE'
                OR (s.status = 'RESERVED' AND s.reservationExpiryTime < CURRENT_TIMESTAMP)
              )
        """)
    int reserveSeats(
            @Param("ids") List<UUID> ids,
            @Param("status") SeatStatus status,
            @Param("userId") UUID userId,
            @Param("expiryTime") LocalDateTime expiryTime,
            @Param("seatReservationId") UUID seatReservationId
    );

    @Modifying
    @Query("""
        UPDATE SeatInventory s
        SET s.status = :status,
            s.reservedByUserId = null,
            s.reservationExpiryTime = null
        WHERE s.id IN :ids
          AND (
                s.status = 'RESERVED' AND s.reservedByUserId = :userId
              )
        """)
    int releaseSeats(
            @Param("ids") List<UUID> ids,
            @Param("status") SeatStatus status,
            @Param("userId") UUID userId
    );

    List<SeatInventory> findBySeatReservationIdAndStatus(UUID seatReservationId, SeatStatus status);
}
