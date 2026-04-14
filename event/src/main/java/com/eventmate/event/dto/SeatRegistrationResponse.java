package com.eventmate.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class SeatRegistrationResponse {
    private String status;
    private UUID tenantId;
    private BigDecimal price;
    private List<RegisteredSeatResponse> seatDetails;
    private String eventName;
    private String venueName;
    private LocalDateTime eventStartDateTime;
    private LocalDateTime seatExpirationTime;
    private UUID seatReservationId;
}
