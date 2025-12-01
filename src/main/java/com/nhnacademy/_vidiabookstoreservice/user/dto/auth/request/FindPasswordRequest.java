package com.nhnacademy._vidiabookstoreservice.user.dto.auth.request;

import jakarta.validation.constraints.NotBlank;


// 비밀번호 찾기 요청 dto
public record FindPasswordRequest(
    @NotBlank(message = "아이디(이메일)을 입력해주세요.")
    String email,
    @NotBlank(message = "이름을 입력해주세요.")
    String name,
    @NotBlank(message = "전화번호를 입력해주세요.")
    String phone
){

}
