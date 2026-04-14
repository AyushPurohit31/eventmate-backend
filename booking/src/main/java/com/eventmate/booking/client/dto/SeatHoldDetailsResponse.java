package com.eventmate.booking.client.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatHoldDetailsResponse {
    private UUID seatReservationId;
    private UUID userId;
    private UUID scheduleId;
    private UUID tenantId;

    private String eventName;
    private String venueName;
    private LocalDateTime eventStartDateTime;

    private LocalDateTime expiresAt;

    private BigDecimal totalAmount;

    private List<SeatDetail> seatDetails;
}