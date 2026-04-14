package com.eventmate.booking.config;

import com.eventmate.booking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RedisExpiryListener implements MessageListener {

    @Autowired
    private BookingService bookingService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String key = message.toString();

        if (key.startsWith("booking:expiry:")) {
            String bookingId = key.split(":")[2];

            bookingService.handleExpiry(UUID.fromString(bookingId));
        }
    }
}