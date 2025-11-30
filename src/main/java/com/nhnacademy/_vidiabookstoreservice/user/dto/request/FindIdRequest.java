package com.nhnacademy._vidiabookstoreservice.user.dto.request;

import jakarta.validation.constraints.NotBlank;

// 아이디 찾기 요청 dto
public record FindIdRequest (
    @NotBlank(message = "이름을 입력해주세요.")
    String name,

    @NotBlank(message = "생년월일을 입력해주세요. (예: yyyy-mm-dd)")
    String birthday,

    @NotBlank(message = "전화번호를 입력해주세요.")
    String phone
){}

