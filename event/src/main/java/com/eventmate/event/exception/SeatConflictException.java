package com.eventmate.event.exception;

/**
 * Indicates a business-level conflict when attempting to reserve seats,
 * e.g., one or more seats are already reserved by someone else.
 */
public class SeatConflictException extends SeatReservationException {
    public SeatConflictException(String message) {
        super(message);
    }
}

