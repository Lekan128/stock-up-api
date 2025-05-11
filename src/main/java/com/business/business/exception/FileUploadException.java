package com.business.business.exception;

import org.springframework.http.HttpStatus;

public class FileUploadException extends RuntimeException{
    public static final HttpStatus STATUS = HttpStatus.BAD_GATEWAY;
    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }

}
