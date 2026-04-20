package com.eventmate.booking.client;

import com.eventmate.booking.client.dto.SeatHoldDetailsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "event", path = "/events")
public interface EventClient {

        @GetMapping("/reserve-seat/{seatReservationId}")
         SeatHoldDetailsResponse getSeatHoldDetails(@PathVariable("seatReservationId") UUID seatReservationId);
}
