package com.eventmate.event.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "venues")
@Getter
@Setter
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID tenantId;

    private String name;

    private Double latitude;
    private Double longitude;

    private String address;
    private String city;
    private String state;
    private String country;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VenueSeat> seats;

    @OneToMany(mappedBy = "venue")
    private List<EventSchedule> schedules;
}
