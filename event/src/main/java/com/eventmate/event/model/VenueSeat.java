package com.eventmate.event.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "venue_seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"venue_id", "section", "row_number", "seat_number"}
                )
        }
)
@Getter
@Setter
public class VenueSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    private String section;
    private String rowNumber;
    private Integer seatNumber;
}