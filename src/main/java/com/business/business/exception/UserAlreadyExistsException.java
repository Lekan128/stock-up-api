package com.business.business.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends RuntimeException {
    public static final HttpStatus STATUS = HttpStatus.CONFLICT;
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
