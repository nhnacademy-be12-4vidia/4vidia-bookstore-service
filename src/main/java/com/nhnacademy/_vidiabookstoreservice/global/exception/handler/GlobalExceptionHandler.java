package com.nhnacademy._vidiabookstoreservice.global.exception.handler;

import com.nhnacademy._vidiabookstoreservice.global.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public ProblemDetail handleBaseException(BaseException e){
        ProblemDetail pd = ProblemDetail.forStatus(e.getErrorCode().getStatus());
        pd.setTitle(e.getErrorCode().getName()); // ex. POINT_NOT_FOUND - 백엔드 로그
        pd.setProperty("code", e.getErrorCode().getCode()); // ex. "P001" - 프론트 분기
        pd.setDetail(e.getFormattedMessage());
        pd.setProperty("timestamp", LocalDate.now());
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception e){
        log.error("Unexpected error", e);
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("INTERNAL_SERVER_ERROR");
        pd.setDetail("서버 오류가 발생했습니다.");
        pd.setProperty("timestamp", LocalDateTime.now());
        return pd;
    }
}