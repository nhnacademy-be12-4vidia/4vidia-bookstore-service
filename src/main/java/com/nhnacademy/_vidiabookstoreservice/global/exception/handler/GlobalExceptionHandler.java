package com.nhnacademy._vidiabookstoreservice.global.exception.handler;

import com.nhnacademy._vidiabookstoreservice.global.exception.*;
import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.user.exception.AlreadyResignedUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({DataIntegrityViolationException.class, NotFoundException.class, NoSuchElementException.class})
    public ProblemDetail handleDataIntegrityViolationException(RuntimeException e) {
        log.warn(e.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            e.getMessage()
        );
        problemDetail.setTitle("NotFound error");

        return problemDetail;
    }


    @ExceptionHandler({AlreadyExistsException.class, NotEnoughException.class})
    public ProblemDetail handleBadRequest(RuntimeException e) {
        log.warn(e.getMessage());

        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, // 409
                e.getMessage()
        );
        detail.setTitle("Conflict error");
        return detail;
    }

    @ExceptionHandler(MismatchException.class)
    public ProblemDetail mismatchException(MismatchException e){
        log.warn(e.getMessage());

        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                e.getMessage()
        );
        detail.setTitle("Forbidden error");
        return detail;
    }

    @ExceptionHandler({RequiredException.class, InvalidException.class})
    public ProblemDetail requiredException(RequiredException e){
        log.warn(e.getMessage());

        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
        );
        detail.setTitle("Bad Request error");
        return detail;
    }
}
