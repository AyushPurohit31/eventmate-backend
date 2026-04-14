package com.eventmate.event.dto;

import com.eventmate.event.model.EventCategory;
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
public class UpdateEventRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private EventCategory eventCategory;

    private String imageUrl;

    private String bannerImageUrl;
}
