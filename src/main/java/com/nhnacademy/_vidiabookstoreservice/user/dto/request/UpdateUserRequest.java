package com.nhnacademy._vidiabookstoreservice.user.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 유저 정보 수정용 dto
 */
public record UpdateUserRequest (
        @NotBlank(message = "이름은 필수 입력입니다.")
        String name,

        @NotBlank(message = "전화번호는 필수 입력입니다.")
        @Pattern(
                regexp = "^01[0-9]\\d{3,4}\\d{4}$",
                message = "전화번호 형식이 올바르지 않습니다. 예) 01012345678"
        )
        String phone
){
}
