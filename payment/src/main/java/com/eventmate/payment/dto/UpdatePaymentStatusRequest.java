package com.eventmate.payment.dto;

import com.eventmate.payment.model.PaymentStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdatePaymentStatusRequest {
    private UUID paymentId;
    private PaymentStatus paymentStatus;
}
