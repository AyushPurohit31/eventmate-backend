package com.eventmate.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterVenueSeatRequest {

    @NotBlank
    private String section;

    @NotBlank
    private String rowNumber;

    @NotBlank
    private Integer seatNumber;
}

