package com.eventmate.booking.client;

import com.eventmate.booking.client.dto.SeatHoldDetailsResponse;
import com.eventmate.booking.exception.ExternalServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class EventClient {

        private static final String BASE_URL = "http://localhost:8083/events";
        private final RestTemplate restTemplate;

        public EventClient(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
        }

        public SeatHoldDetailsResponse getSeatHoldDetails(UUID seatReservationId) {
            try {
                String url = BASE_URL + "/reserve-seat/" + seatReservationId;
                return restTemplate.getForObject(url, SeatHoldDetailsResponse.class, seatReservationId);
            } catch (Exception e){
                throw new ExternalServiceException("Failed to fetch seat hold details", e);
            }
        }
}
