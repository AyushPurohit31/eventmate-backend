package com.eventmate.event.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisteredSeatResponse {
    private UUID seatInventoryId;
    private String seatRow;
    private Integer seatNumber;
    private BigDecimal price;
    private Enum status;
}
