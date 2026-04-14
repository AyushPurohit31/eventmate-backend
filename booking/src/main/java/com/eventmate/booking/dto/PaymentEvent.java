package com.eventmate.booking.dto;

import lombok.Data;

import java.util.UUID;

/**
 * Local copy of the PaymentEvent payload emitted by the payment service.
 */
@Data
public class PaymentEvent {
    private UUID messageEventId;
    private UUID bookingId;
    private UUID paymentId;
    private String eventType;
    private String userName;
    private String userEmail;
}
