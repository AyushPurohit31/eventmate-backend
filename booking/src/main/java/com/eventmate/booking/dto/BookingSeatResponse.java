package com.eventmate.booking.dto;

import com.eventmate.booking.model.BookingSeatStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
public class BookingSeatResponse {

    private UUID seatInventoryId;

    private String seatLabel;

    private BigDecimal price;

    private BookingSeatStatus status;
}
