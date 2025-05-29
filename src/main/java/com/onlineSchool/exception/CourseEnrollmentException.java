package com.onlineSchool.exception;

public class CourseEnrollmentException extends RuntimeException {
    public CourseEnrollmentException(String message) {
        super(message);
    }

    public CourseEnrollmentException(String message, Throwable cause) {
        super(message, cause);
    }
} 