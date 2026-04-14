package com.eventmate.booking.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "user_email", nullable = false)
    private String email;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "scheduleId", nullable = false)
    private UUID scheduleId;

    @Column(name = "event-name", nullable = false)
    private String eventNameSnapShot;

    @Column(name = "event-venue", nullable = false)
    private String eventVenueSnapShot;

    @Column(name = "event-time", nullable = false)
    private LocalDateTime eventTimeSnapShot;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "booking_status")
    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "seat_reservation_id", unique = true, nullable = false)
    private UUID seatReservationId;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Builder.Default
    @OneToMany(
            mappedBy = "booking",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<BookingSeat> seats = new ArrayList<>();
}
