package com.eventmate.tenant.exception;

public class DuplicateSlugException extends RuntimeException {
    public DuplicateSlugException(String message) {
        super(message);
    }
}

