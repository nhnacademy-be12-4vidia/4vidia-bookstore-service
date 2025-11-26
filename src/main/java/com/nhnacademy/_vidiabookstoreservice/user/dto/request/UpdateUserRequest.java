package com.nhnacademy._vidiabookstoreservice.user.dto.request;


/**
 * 유저 정보 수정용 dto
 */

public record UpdateUserRequest (
        String name,
        String phone
//        LocalDate birthDate // yyyy-MM-dd
){
}
