package com.nhnacademy._vidiabookstoreservice.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @Size(max = 20)
        @NotBlank
        String alias,// 별칭
        @Size(max = 30)
        @NotBlank(message = "도로명 주소는 필수 입력값입니다.")
        String roadAddress,//도로명 주소
        @Size(max = 5)
        @NotBlank(message = "우편번호는 필수 입력값입니다.")
        String zipCode, // 우편번호
        @Size(max = 30)
        String addressDetail
){

}
