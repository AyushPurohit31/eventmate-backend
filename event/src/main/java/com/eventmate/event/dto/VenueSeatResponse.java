package com.eventmate.event.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VenueSeatResponse {
    private UUID id;
    private String section;
    private String rowNumber;
    private Integer seatNumber;
}

