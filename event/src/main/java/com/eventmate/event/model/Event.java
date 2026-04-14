package com.eventmate.event.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID tenantId;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private EventCategory eventType;

    private String imageUrl;
    private String bannerImageUrl;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
