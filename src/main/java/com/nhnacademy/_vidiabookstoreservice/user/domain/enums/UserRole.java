package com.nhnacademy._vidiabookstoreservice.user.domain.enums;


import lombok.Getter;

@Getter
public enum UserRole {
    USER(0),
    ADMIN(1);

    private final int code;

    UserRole(int code){
        this.code = code;
    }
    public static UserRole of(int code){
        for(UserRole role : UserRole.values()){
            if(role.code == code){
                return role;
            }
        }
        throw new IllegalArgumentException("일치하는 권한이 없습니다."+code);
    }
}
