package com.eventmate.booking.dto;

import com.eventmate.booking.model.BookingStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class BookingSummaryResponse {

    private UUID bookingId;

    private String eventTitle;

    private String venueName;

    private LocalDateTime showTime;

    private BookingStatus status;

    private BigDecimal totalAmount;

    private List<String> seatLabels;
}