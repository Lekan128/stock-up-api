package com.business.business.exception;

import org.springframework.http.HttpStatus;

//@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthenticationException extends RuntimeException {
    public static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

