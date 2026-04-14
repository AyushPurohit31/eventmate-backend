package com.eventmate.booking.service;

import com.eventmate.booking.client.UserClient;
import com.eventmate.booking.client.dto.SeatDetail;
import com.eventmate.booking.client.dto.UserResponse;
import com.eventmate.booking.dto.BookingSeatResponse;
import com.eventmate.booking.dto.RegisterBookingRequest;
import com.eventmate.booking.dto.RegisterBookingResponse;
import com.eventmate.booking.client.dto.SeatHoldDetailsResponse;
import com.eventmate.booking.exception.BookingFailureException;
import com.eventmate.booking.model.Booking;
import com.eventmate.booking.model.BookingSeat;
import com.eventmate.booking.model.BookingSeatStatus;
import com.eventmate.booking.model.BookingStatus;
import com.eventmate.booking.repository.BookingRepository;
import com.eventmate.booking.repository.BookingSeatRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingUtil {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final StringRedisTemplate redisTemplate;
    private final UserClient userClient;

    public BookingUtil(BookingRepository bookingRepository,
                       BookingSeatRepository bookingSeatRepository,
                       StringRedisTemplate redisTemplate,
                       UserClient userClient) {
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.redisTemplate = redisTemplate;
        this.userClient = userClient;
    }

    @Transactional
    RegisterBookingResponse createBookingWithSeats(
            RegisterBookingRequest request,
            SeatHoldDetailsResponse holdDetails) {

        List<SeatDetail> seatDetails = holdDetails.getSeatDetails();

        LocalDateTime now = LocalDateTime.now();

        UserResponse user = null;
        try {
            user = userClient.getUser(request.getUserId());
        } catch (Exception e) {
            throw new BookingFailureException("Failed to fetch user details for userId: " + request.getUserId(), e);
        }

        Booking booking = Booking.builder()
                .userId(request.getUserId())
                .email(user.getEmail())
                .userName(user.getFirstName()+ " " + user.getLastName())
                .scheduleId(holdDetails.getScheduleId())
                .tenantId(holdDetails.getTenantId())
                .eventNameSnapShot(holdDetails.getEventName())
                .eventVenueSnapShot(holdDetails.getVenueName())
                .eventTimeSnapShot(holdDetails.getEventStartDateTime())
                .amount(holdDetails.getTotalAmount())
                .createdAt(now)
                .updatedAt(now)
                .bookingStatus(BookingStatus.PENDING)
                .expiresAt(holdDetails.getExpiresAt())
                .seatReservationId(holdDetails.getSeatReservationId())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        List<BookingSeat> bookingSeats = seatDetails.stream()
                .map(seat -> BookingSeat.builder()
                        .booking(savedBooking)
                        .seatInventoryId(seat.getSeatInventoryId())
                        .seatLabelSnapshot(seat.getSeatRow() + seat.getSeatNumber())
                        .priceSnapshot(seat.getPrice())
                        .status(BookingSeatStatus.HELD)
                        .createdAt(now)
                        .updatedAt(now)
                        .build())
                .toList();

        bookingSeatRepository.saveAll(bookingSeats);

        if (savedBooking.getExpiresAt() != null) {
            Duration ttl = Duration.between(LocalDateTime.now(), savedBooking.getExpiresAt());
            if (!ttl.isNegative() && !ttl.isZero()) {
                redisTemplate.opsForValue().set(
                        "booking:expiry:" + savedBooking.getId(),
                        "1",
                        ttl
                );
            }
        }

        List<BookingSeatResponse> seatResponses = bookingSeats.stream()
                .map(seat -> BookingSeatResponse.builder()
                        .seatInventoryId(seat.getSeatInventoryId())
                        .seatLabel(seat.getSeatLabelSnapshot())
                        .price(seat.getPriceSnapshot())
                        .status(seat.getStatus())
                        .build())
                .toList();

        return RegisterBookingResponse.builder()
                .userId(savedBooking.getUserId())
                .userName(savedBooking.getUserName())
                .userEmail(savedBooking.getEmail())
                .bookingId(savedBooking.getId())
                .status(savedBooking.getBookingStatus())
                .eventTitle(savedBooking.getEventNameSnapShot())
                .venueName(savedBooking.getEventVenueSnapShot())
                .showTime(savedBooking.getEventTimeSnapShot())
                .totalAmount(savedBooking.getAmount())
                .seats(seatResponses)
                .createdAt(savedBooking.getCreatedAt())
                .expiresAt(savedBooking.getExpiresAt())
                .build();
    }

    public RegisterBookingResponse mapToRegisterBookingResponse(Booking booking) {
        return RegisterBookingResponse.builder()
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .userEmail(booking.getEmail())
                .userName(booking.getUserName())
                .status(booking.getBookingStatus())
                .eventTitle(booking.getEventNameSnapShot())
                .venueName(booking.getEventVenueSnapShot())
                .showTime(booking.getEventTimeSnapShot())
                .totalAmount(booking.getAmount())
                .seats(booking.getSeats().stream()
                        .map(seat -> BookingSeatResponse.builder()
                                .seatInventoryId(seat.getSeatInventoryId())
                                .seatLabel(seat.getSeatLabelSnapshot())
                                .price(seat.getPriceSnapshot())
                                .status(seat.getStatus())
                                .build())
                        .toList())
                .createdAt(booking.getCreatedAt())
                .expiresAt(booking.getExpiresAt())
                .paymentId(booking.getPaymentId())
                .paymentStatus(booking.getPaymentStatus())
                .build();
    }

}
