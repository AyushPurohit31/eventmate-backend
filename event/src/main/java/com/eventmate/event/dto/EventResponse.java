package com.eventmate.event.dto;

import com.eventmate.event.model.EventCategory;
import com.eventmate.event.model.EventStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {

    private UUID id;
    private UUID tenantId;
    private String title;
    private String description;
    private EventCategory eventType;
    private String imageUrl;
    private String bannerImageUrl;
    private EventStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
