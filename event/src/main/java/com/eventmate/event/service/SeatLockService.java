package com.eventmate.event.service;

import com.eventmate.event.dto.LockResult;
import com.eventmate.event.exception.SeatReservationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class SeatLockService {

    private final StringRedisTemplate redisTemplate;
    private final Logger log = LoggerFactory.getLogger(SeatLockService.class);

    public SeatLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public LockResult lockSeats(UUID userId, List<UUID> seatIds, LocalDateTime expiresAt ) {
        Duration ttl = Duration.between(LocalDateTime.now(), expiresAt);
        if (userId == null || seatIds == null || seatIds.isEmpty()) {
            throw new SeatReservationException("Invalid lock request");
        }
        List<String> lockedKeys = new ArrayList<>();
        List<UUID> newlyLocked = new ArrayList<>();
        try {
            for (UUID seatId : seatIds) {
                String key = buildKey(seatId);
                Boolean success = redisTemplate.opsForValue().setIfAbsent(key, userId.toString(), ttl);
                if (Boolean.FALSE.equals(success)) {
                    String currentOwner = redisTemplate.opsForValue().get(key);
                    if (userId.toString().equals(currentOwner)) {
                        continue; // idempotent
                    }
                    throw new SeatReservationException("Seat already locked: " + seatId);
                }
                lockedKeys.add(key);
                newlyLocked.add(seatId);
            }
            return new LockResult(newlyLocked);
        } catch (Exception e) {
            // rollback only acquired locks
            for (String key : lockedKeys) {
                redisTemplate.delete(key);
            }
            throw new SeatReservationException("Failed to lock seats", e);
        }
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

    public boolean isSeatLocked(UUID seatId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(seatId)));
    }

    private String buildKey(UUID seatId) {
        return "seat_lock:" + seatId;
    }
}
