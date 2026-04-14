package com.eventmate.booking.service;

import com.eventmate.booking.dto.BookingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BookingEventProducer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventProducer.class);
    private static final String BOOKING_EVENTS_TOPIC = "booking-events";

    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;

    public BookingEventProducer(KafkaTemplate<String, BookingEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(BookingEvent event) {
        if (event == null) {
            log.warn("Skipping publish of null PaymentEvent");
            return;
        }
        String key = event.getSeatReservationId().toString();
        log.info("Publishing booking event to Kafka. key={}, eventType={}, bookingId={}, seatReservationId={}",
                key, event.getEventType(), event.getBookingId(), event.getSeatReservationId());
        kafkaTemplate.send(BOOKING_EVENTS_TOPIC, key, event);
    }
}
