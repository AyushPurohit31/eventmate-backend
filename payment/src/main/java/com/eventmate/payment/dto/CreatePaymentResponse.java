package com.eventmate.payment.dto;

import com.eventmate.payment.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreatePaymentResponse {
    private UUID paymentId;
    private PaymentStatus status;
}
