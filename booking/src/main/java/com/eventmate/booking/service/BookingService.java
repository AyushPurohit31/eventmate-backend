package com.eventmate.booking.service;

import com.eventmate.booking.client.EventClient;
import com.eventmate.booking.client.PaymentClient;
import com.eventmate.booking.client.dto.PaymentRequest;
import com.eventmate.booking.client.dto.PaymentResponse;
import com.eventmate.booking.client.dto.SeatHoldDetailsResponse;
import com.eventmate.booking.dto.BookingEvent;
import com.eventmate.booking.dto.RegisterBookingRequest;
import com.eventmate.booking.dto.RegisterBookingResponse;
import com.eventmate.booking.exception.BookingFailureException;
import com.eventmate.booking.model.Booking;
import com.eventmate.booking.model.BookingSeatStatus;
import com.eventmate.booking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.eventmate.booking.model.BookingStatus.*;

@Service
public class BookingService {

    private final Logger log = LoggerFactory.getLogger(BookingService.class);
    private final EventClient eventClient;
    private final BookingUtil bookingUtil;
    private final BookingRepository bookingRepository;
    private final PaymentClient paymentClient;
    private final BookingEventProducer bookingEventProducer;

    public BookingService(EventClient eventClient,
                          BookingUtil bookingUtil,
                          BookingRepository bookingRepository,
                          PaymentClient paymentClient,
                          BookingEventProducer bookingEventProducer) {
        this.eventClient = eventClient;
        this.bookingUtil = bookingUtil;
        this.bookingRepository = bookingRepository;
        this.paymentClient = paymentClient;
        this.bookingEventProducer = bookingEventProducer;
    }

    public RegisterBookingResponse registerBooking(RegisterBookingRequest request) {
        log.info("Creating booking for seatReservationId: {}", request.getSeatReservationId());

        Optional<Booking> existing = bookingRepository.findBySeatReservationId(request.getSeatReservationId());
        if (existing.isPresent()) {
            log.info("Booking already exists for seatReservationId: {}, returning existing booking {}",
                    request.getSeatReservationId(), existing.get().getId());
            return bookingUtil.mapToRegisterBookingResponse(existing.get());
        }

        try {
            SeatHoldDetailsResponse holdDetails = eventClient.getSeatHoldDetails(request.getSeatReservationId());
            if (!request.getUserId().equals(holdDetails.getUserId())) {
                throw new BookingFailureException("Seat reservation does not belong to user");
            }
            if (holdDetails.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new BookingFailureException("Seat reservation expired");
            }

            RegisterBookingResponse bookingResponse = bookingUtil.createBookingWithSeats(request, holdDetails);

            return processPaymentForBooking(bookingResponse);
        } catch (Exception e) {
            log.error("Booking creation failed for seatReservationId: {}", request.getSeatReservationId(), e);
            throw new BookingFailureException("Failed to create booking", e);
        }
    }

    private RegisterBookingResponse processPaymentForBooking(RegisterBookingResponse bookingResponse) {
        try {
            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .bookingId(bookingResponse.getBookingId())
                    .userId(bookingResponse.getUserId())
                    .userName(bookingResponse.getUserName())
                    .userEmail(bookingResponse.getUserEmail())
                    .amount(bookingResponse.getTotalAmount())
                    .expiresAt(bookingResponse.getExpiresAt())
                    .build();

            PaymentResponse paymentResponse = paymentClient.createPayment(paymentRequest);
            UUID paymentId = paymentResponse != null ? paymentResponse.getPaymentId() : null;
            String paymentStatus = paymentResponse != null ? paymentResponse.getStatus() : null;

            Booking booking = bookingRepository.findById(bookingResponse.getBookingId())
                    .orElseThrow(() -> new BookingFailureException("Booking not found after creation"));
            booking.setPaymentId(paymentId);
            booking.setPaymentStatus(paymentStatus);
            bookingRepository.save(booking);

            log.info("Payment created for bookingId: {}, paymentId: {}, status: {}",
                    booking.getId(), paymentId, paymentStatus);

            return bookingUtil.mapToRegisterBookingResponse(booking);
        } catch (Exception e) {
            log.error("Failed to create payment for bookingId: {}", bookingResponse.getBookingId(), e);
            return bookingResponse;
        }
    }

    @Transactional
    public void handleExpiry(UUID bookingId) {
        log.info("Handling expiry for bookingId: {}", bookingId);
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            log.warn("Booking not found for expiry handling: {}", bookingId);
            return;
        }
        Booking booking = optionalBooking.get();
        if (booking.getBookingStatus() == PENDING && booking.getExpiresAt() != null
                && booking.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.info("Expiring bookingId: {}", bookingId);
            booking.setBookingStatus(FAILED);
            booking.setUpdatedAt(LocalDateTime.now());
            booking.getSeats().forEach(seat -> {
                seat.setStatus(BookingSeatStatus.CANCELLED);
                seat.setUpdatedAt(LocalDateTime.now());
            });
            bookingRepository.save(booking);
            BookingEvent event = new BookingEvent();
            event.setBookingId(bookingId);
            event.setSeatReservationId(booking.getSeatReservationId());
            event.setEventType("booking-failed");
            event.setUserId(booking.getUserId());
            bookingEventProducer.publish(event);
        } else {
            log.info("BookingId: {} is not eligible for expiry. Current status: {}, expiresAt: {}",
                    bookingId, booking.getBookingStatus(), booking.getExpiresAt());
        }
    }
}
