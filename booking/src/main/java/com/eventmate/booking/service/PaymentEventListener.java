package com.eventmate.booking.service;

import com.eventmate.booking.dto.BookingEvent;
import com.eventmate.booking.dto.PaymentEvent;
import com.eventmate.booking.model.Booking;
import com.eventmate.booking.model.BookingSeatStatus;
import com.eventmate.booking.model.BookingStatus;
import com.eventmate.booking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final BookingRepository bookingRepository;
    private final BookingEventProducer bookingEventProducer;
    private final StringRedisTemplate redisTemplate;

    public PaymentEventListener(BookingRepository bookingRepository,
                                BookingEventProducer bookingEventProducer,
                                StringRedisTemplate redisTemplate) {
        this.bookingRepository = bookingRepository;
        this.redisTemplate = redisTemplate;
        this.bookingEventProducer = bookingEventProducer;
    }

    @KafkaListener(topics = "payment-events", groupId = "booking-service")
    @Transactional
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("RECEIVED EVENT: {}", event);
        if (event == null) {
            log.warn("Received null PaymentEvent. Skipping.");
            return;
        }

        UUID bookingId = event.getBookingId();
        UUID paymentId = event.getPaymentId();
        String eventType = event.getEventType();

        log.info("Received payment event: bookingId={}, paymentId={}, eventType={}",
                bookingId, paymentId, eventType);

        if (bookingId == null) {
            throw new IllegalArgumentException("Invalid bookingId");
        }

        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            throw new RuntimeException("Booking not found for payment event");
        }

        Booking booking = optionalBooking.get();
        BookingStatus currentStatus = booking.getBookingStatus();

        LocalDateTime now = LocalDateTime.now();

        if (booking.getExpiresAt() == null || booking.getExpiresAt().isBefore(now)) {
            log.warn("Booking {} expired before payment success", bookingId);

            booking.setBookingStatus(BookingStatus.FAILED);
            booking.setPaymentStatus("refund-required");
            booking.setUpdatedAt(now);
            booking.getSeats().forEach(seat -> {
                seat.setStatus(BookingSeatStatus.CANCELLED);
                seat.setUpdatedAt(LocalDateTime.now());
            });

            bookingRepository.save(booking);
            return;
        }

        // Idempotency: if booking is already in a final state, don't change it.
        if (isFinal(currentStatus)) {
            log.info("Booking {} already in final status {}. Skipping update for event {}.",
                    bookingId, currentStatus, eventType);
            return;
        }

        // Determine target status based on payment event
        BookingStatus targetStatus;
        String bookingEventType;
        if ("payment-success".equalsIgnoreCase(eventType)) {
            targetStatus = BookingStatus.CONFIRMED;
            bookingEventType = "booking-confirmed";
        } else if ("payment-failed".equalsIgnoreCase(eventType)) {
            targetStatus = BookingStatus.FAILED;
            bookingEventType = "booking-failed";
        } else {
            log.warn("Unknown payment eventType '{}' for booking {}. Skipping.", eventType, bookingId);
            return;
        }

        // Idempotency: if the target status is same as current, nothing to do.
        if (currentStatus == targetStatus) {
            log.info("Booking {} already in status {}. Skipping update for event {}.",
                    bookingId, currentStatus, eventType);
            return;
        }

        // Apply status update and store payment info
        booking.setBookingStatus(targetStatus);
        booking.setPaymentId(paymentId);
        booking.setPaymentStatus(eventType);
        booking.setUpdatedAt(LocalDateTime.now());

        // Also update all booking seats to reflect final booking status
        if (booking.getSeats() != null) {
            BookingSeatStatus seatStatus = targetStatus == BookingStatus.CONFIRMED
                    ? BookingSeatStatus.BOOKED
                    : BookingSeatStatus.CANCELLED;
            booking.getSeats().forEach(seat -> {
                seat.setStatus(seatStatus);
                seat.setUpdatedAt(LocalDateTime.now());
            });
        }

        bookingRepository.save(booking);
        redisTemplate.delete("booking:expiry:" + booking.getId());
        BookingEvent bookingEvent = getBookingEvent(event, bookingEventType, booking);
        bookingEventProducer.publish(bookingEvent);

        log.info("Booking {} updated to status {} based on payment event {}.",
                bookingId, targetStatus, eventType);
    }

    private static BookingEvent getBookingEvent(PaymentEvent event, String bookingEventType, Booking booking) {
        BookingEvent bookingEvent = new BookingEvent();
        bookingEvent.setEventType(bookingEventType);
        bookingEvent.setMessageEventId(UUID.randomUUID());
        bookingEvent.setBookingId(booking.getId());
        bookingEvent.setSeatReservationId(booking.getSeatReservationId());
        bookingEvent.setUserId(booking.getUserId());
        bookingEvent.setUserEmail(event.getUserEmail());
        bookingEvent.setUserName(event.getUserName());
        bookingEvent.setEventTitle(booking.getEventNameSnapShot());
        bookingEvent.setEventLocation(booking.getEventVenueSnapShot());
        bookingEvent.setEventDateTime(booking.getEventTimeSnapShot());
        return bookingEvent;
    }

    private boolean isFinal(BookingStatus status) {
        return status == BookingStatus.CONFIRMED
                || status == BookingStatus.FAILED
                || status == BookingStatus.CANCELLED;
    }
}
