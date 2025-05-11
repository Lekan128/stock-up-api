package com.business.business.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends RuntimeException {
    public static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
