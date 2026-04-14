package com.eventmate.event.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventScheduleResponse {
    private UUID id;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private EventResponse event;
    private VenueResponse venue;
}
