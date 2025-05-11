package com.business.business.exception;

import org.springframework.http.HttpStatus;

public record ErrorResponse (String errorMessage, HttpStatus httpStatus) {
}
