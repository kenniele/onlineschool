package com.onlineSchool.exception;

public class WebinarException extends RuntimeException {
    public WebinarException(String message) {
        super(message);
    }

    public WebinarException(String message, Throwable cause) {
        super(message, cause);
    }
} 