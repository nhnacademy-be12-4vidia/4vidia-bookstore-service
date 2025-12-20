package com.nhnacademy._vidiabookstoreservice.admin.exception;

public class BookSearchFailedException extends RuntimeException {
    
    public BookSearchFailedException(String message) {
        super(message);
    }
    
    public BookSearchFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}