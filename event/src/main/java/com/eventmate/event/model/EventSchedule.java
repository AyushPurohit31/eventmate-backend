package com.eventmate.event.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "event_schedules")
@Getter
@Setter
public class EventSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    private Integer availablePasses;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
    private List<SeatInventory> seats;

    private LocalDateTime createdAt;
}
