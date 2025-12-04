package com.nhnacademy._vidiabookstoreservice.global.exception.handler;

import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            e.getMessage()
        );

        problemDetail.setTitle("Database error");

        return problemDetail;
    }


    @ExceptionHandler(AlreadyExistsException.class)
    public ProblemDetail handleBadRequest(AlreadyExistsException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, // 409
                e.getMessage()
        );
        detail.setTitle("Conflict error");
        return detail;
    }
}
