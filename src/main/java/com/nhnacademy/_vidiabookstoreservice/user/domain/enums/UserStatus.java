package com.nhnacademy._vidiabookstoreservice.user.domain.enums;


import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE(1), //정상
    DORMANT(2), //휴면
    DELETED(3) ;

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
