package com.eventmate.booking.dto;

import com.eventmate.booking.model.BookingStatus;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterBookingResponse {

    private UUID bookingId;

    private UUID userId;

    private String userEmail;

    private String userName;

    private BookingStatus status;

    private String eventTitle;

    private String venueName;

    private LocalDateTime showTime;

    private BigDecimal totalAmount;

    private List<BookingSeatResponse> seats;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private UUID paymentId;

    private String paymentStatus;
}
