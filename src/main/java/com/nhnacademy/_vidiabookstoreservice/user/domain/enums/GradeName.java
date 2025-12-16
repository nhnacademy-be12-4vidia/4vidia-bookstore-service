package com.nhnacademy._vidiabookstoreservice.user.domain.enums;

import lombok.Getter;

@Getter
public enum GradeName {
    WELCOME(0),
    REGULAR(1),
    ROYAL(2),
    GOLD(3),
    PLATINUM(4);

    private final int code;

    GradeName(int code){
        this.code = code;
    }
    public static GradeName of(int code){
        for(GradeName gradeName : GradeName.values()){
            if(gradeName.code == code){
                return gradeName;
            }
        }
        throw new IllegalArgumentException("일치하는 등급이름이 없습니다."+code);
    }
}
