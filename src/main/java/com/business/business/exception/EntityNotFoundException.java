package com.business.business.exception;

import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends RuntimeException {
    public static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
