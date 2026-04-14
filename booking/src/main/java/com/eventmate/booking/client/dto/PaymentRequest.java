package com.eventmate.booking.client.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    @NotNull
    private UUID bookingId;
    @NotNull
    private UUID userId;
    @NotNull
    private String userName;
    @NotNull
    private String userEmail;
    @NotNull
    private BigDecimal amount;
    @NotNull
    private LocalDateTime expiresAt;
}
