package com.nhnacademy._vidiabookstoreservice.global.exception.handler;

import com.netflix.discovery.converters.Auto;
import com.nhnacademy._vidiabookstoreservice.global.exception.*;
import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.user.exception.AlreadyResignedUserException;
import com.nhnacademy._vidiabookstoreservice.user.exception.AuthCodeExpiredException;
import com.nhnacademy._vidiabookstoreservice.user.exception.InvalidAuthCodeException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({DataIntegrityViolationException.class, NotFoundException.class})
    public ProblemDetail handleDataIntegrityViolationException(RuntimeException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            e.getMessage()
        );

        problemDetail.setTitle("NotFound error");

        return problemDetail;
    }


    @ExceptionHandler({AlreadyExistsException.class, NotEnoughException.class, AlreadyResignedUserException.class})
    public ProblemDetail handleBadRequest(RuntimeException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, // 409
                e.getMessage()
        );
        detail.setTitle("Conflict error");
        return detail;
    }

    @ExceptionHandler(MismatchException.class)
    public ProblemDetail mismatchException(MismatchException e){
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                e.getMessage()
        );
        detail.setTitle("Mismatch Exception");
        return detail;
    }

    @ExceptionHandler(RequiredException.class)
    public ProblemDetail requiredException(RequiredException e){
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
        );
        detail.setTitle("RequiredException");
        return detail;
    }



}
