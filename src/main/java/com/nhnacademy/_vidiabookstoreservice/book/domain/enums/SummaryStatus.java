package com.nhnacademy._vidiabookstoreservice.book.domain.enums;

import lombok.Getter;

@Getter
public enum SummaryStatus {
    READY(0), RUNNING(1), FAILED(2);

    private final int code;

    SummaryStatus(int code) { this.code = code; }

    public static SummaryStatus of(int code) {
        for (SummaryStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        } throw new IllegalArgumentException("일치하는 리뷰 요약 상태코드가 없습니다: " + code);
    }
}
