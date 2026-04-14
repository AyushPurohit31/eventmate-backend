package com.eventmate.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class LockResult {
    private final List<UUID> newlyLockedSeatIds;
}