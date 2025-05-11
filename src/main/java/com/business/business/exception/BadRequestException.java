package com.business.business.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends RuntimeException {
    public static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
