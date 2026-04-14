package com.eventmate.payment.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PaymentEvent {
    private UUID messageEventId;
    private UUID bookingId;
    private UUID paymentId;
    private String eventType;
    private String userName;
    private String userEmail;
}
