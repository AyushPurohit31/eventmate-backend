package com.eventmate.booking.exception;

/**
 * Thrown when a booking cannot be created or persisted after seats have been reserved.
 */
public class BookingFailureException extends RuntimeException {

    public BookingFailureException(String message) {
        super(message);
    }

    public BookingFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}

