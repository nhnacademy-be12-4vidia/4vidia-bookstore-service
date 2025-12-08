package com.nhnacademy._vidiabookstoreservice.global.exception.handler;

import com.nhnacademy._vidiabookstoreservice.global.exception.*;
import com.nhnacademy._vidiabookstoreservice.global.exception.AlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.user.exception.AlreadyResignedUserException;
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

    /**
     * 휴면 인증: 인증코드 만료 / 인증코드 불일치 처리
     */
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleExpired(IllegalStateException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "인증 코드가 만료되었습니다. 다시 발송해주세요."
        );
        detail.setTitle("AuthCode Expired");
        return detail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalid(IllegalArgumentException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "잘못된 인증 코드입니다. 다시 입력해주세요."
        );
        detail.setTitle("Invalid AuthCode");
        return detail;
    }



}
