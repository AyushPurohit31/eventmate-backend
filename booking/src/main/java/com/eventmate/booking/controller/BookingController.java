package com.eventmate.booking.controller;

import com.eventmate.booking.dto.RegisterBookingRequest;
import com.eventmate.booking.dto.RegisterBookingResponse;
import com.eventmate.booking.service.BookingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final Logger log = LoggerFactory.getLogger(BookingController.class);

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterBookingResponse> registerBooking(@Valid @RequestBody RegisterBookingRequest request) {
        log.info("Received booking registration request for userId: {} and scheduleId: {}", request.getUserId(), request.getScheduleId());
        RegisterBookingResponse response = bookingService.registerBooking(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
