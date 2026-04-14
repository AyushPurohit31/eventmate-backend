package com.eventmate.event.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatRegistrationRequest {

    @NotNull
    private UUID userId;

    @NotEmpty
    private List<UUID> seatInventoryIds;
}
