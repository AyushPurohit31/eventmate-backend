package com.eventmate.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterBookingRequest {

    @NotNull
    private UUID scheduleId;

    @NotNull
    private UUID userId;

    @NotNull
    private UUID seatReservationId;
}
