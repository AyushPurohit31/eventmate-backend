package com.eventmate.booking.exception;

public class PaymentCreationFailure extends RuntimeException {
    public PaymentCreationFailure(String message) {
        super(message);
    }
  public PaymentCreationFailure(String message, Exception e) {
    super(message, e);
  }
}
