package com.nhnacademy._vidiabookstoreservice.user.dto.user.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;


// 회원가입 요청 DTO
public record UserSignupRequest(

        @NotBlank(message = "이메일은 필수 입력입니다.")
        @Size(max=50)
        String email,

        @NotBlank(message = "비밀번호는 필수 입력입니다.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,20}$",
                message = "비밀번호는 소문자, 숫자, 특수문자를 포함한 8~20자여야 합니다."
        )
        String password,

        @NotBlank(message = "이름은 필수 입력입니다.")
        String name,

        @NotBlank(message = "전화번호는 필수 입력입니다.")
        @Pattern(
                regexp = "^01[0-9]\\d{3,4}\\d{4}$",
                message = "전화번호 형식이 올바르지 않습니다. 예) 01012345678"
        )
        String phone,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @Past
        LocalDate birthDate
) {

}
