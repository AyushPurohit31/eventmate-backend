package com.eventmate.notification.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BookingEvent {
    private UUID messageEventId;
    private UUID bookingId;
    private String eventType;
    private UUID seatReservationId;
    private UUID userId;
    private String userEmail;
    private String userName;
    private String eventTitle;
    private String eventLocation;
    private LocalDateTime eventDateTime;
}
