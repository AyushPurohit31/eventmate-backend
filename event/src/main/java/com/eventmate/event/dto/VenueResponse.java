package com.eventmate.event.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueResponse {
    private UUID id;
    private UUID tenantId;
    private String name;
    private Double latitude;
    private Double longitude;
    private String address;
    private String city;
    private String state;
    private String country;
    private List<VenueSeatResponse> seats;
    private List<EventScheduleResponse> schedules;
}
