package com.eventmate.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentRequest {

    @NotNull
    private UUID bookingId;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private UUID userId;

    @NotNull
    private String userName;

    @NotNull
    private String userEmail;

    @NotNull
    private LocalDateTime expiresAt;
}
