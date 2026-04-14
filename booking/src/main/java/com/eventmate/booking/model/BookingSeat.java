package com.eventmate.booking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "booking_seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // Reference to Event Service seat_inventory.id
    @Column(name = "seat_inventory_id", nullable = false)
    private UUID seatInventoryId;

    // Snapshot fields
    @Column(name = "seat_label", nullable = false)
    private String seatLabelSnapshot;  // e.g. "A1"

    @Column(nullable = false)
    private BigDecimal priceSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingSeatStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}