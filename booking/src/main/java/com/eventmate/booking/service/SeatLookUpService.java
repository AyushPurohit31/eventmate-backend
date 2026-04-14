package com.eventmate.booking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SeatLookUpService {

    private final StringRedisTemplate redisTemplate;
    private final Logger log = LoggerFactory.getLogger(SeatLookUpService.class);

    public SeatLookUpService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void releaseSeats(UUID userId, List<UUID> seatIds) {
        if (userId == null || seatIds == null || seatIds.isEmpty()) {
            return;
        }
        for (UUID seatId : seatIds) {
            String key = buildKey(seatId);
            try {
                String currentOwner = redisTemplate.opsForValue().get(key);
                if (userId.toString().equals(currentOwner)) {
                    redisTemplate.delete(key);
                }
            } catch (Exception e) {
                log.error("Failed to release lock for seatId={}", seatId, e);
            }
        }
    }

    public boolean validateSeats(UUID userId, List<UUID> seatIds) {
        if (userId == null || seatIds == null || seatIds.isEmpty()) {
            return false;
        }
        for (UUID seatId : seatIds) {
            String key = buildKey(seatId);
            try {
                String currentOwner = redisTemplate.opsForValue().get(key);
                if (!userId.toString().equals(currentOwner)) {
                    log.debug("Seat {} is not locked by user {} (owner={})", seatId, userId, currentOwner);
                    return false;
                }
            } catch (Exception e) {
                log.error("Failed to validate lock for seatId={}", seatId, e);
                return false;
            }
        }
        return true;
    }

    private String buildKey(UUID seatId) {
        return "seat_lock:" + seatId;
    }
}
