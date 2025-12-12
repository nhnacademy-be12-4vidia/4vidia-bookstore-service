package com.nhnacademy._vidiabookstoreservice.user.domain.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE(0), //정상
    DORMANT(1), //휴면
    DELETED(2),
    TEMP(3);

    private final int code;

    UserStatus(int code){
        this.code = code;
    }
    public static UserStatus of(int code) {
        for (UserStatus status : UserStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("일치하는 권한이 없습니다." + code);
    }
}
