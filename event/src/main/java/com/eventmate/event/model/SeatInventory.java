package com.eventmate.event.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "seat_inventory")
@Getter
@Setter
public class SeatInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private EventSchedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_seat_id", nullable = false)
    private VenueSeat venueSeat;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    private BigDecimal price;

    private LocalDateTime reservationExpiryTime;

    private UUID reservedByUserId;

    private UUID seatReservationId;
}
