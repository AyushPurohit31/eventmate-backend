package com.eventmate.event.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatDetail {
    private UUID seatInventoryId;
    private String seatRow;
    private Integer seatNumber;
    private BigDecimal price;
    private String status;
}
