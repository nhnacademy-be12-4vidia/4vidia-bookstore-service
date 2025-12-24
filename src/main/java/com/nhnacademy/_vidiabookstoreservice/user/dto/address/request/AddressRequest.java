package com.nhnacademy._vidiabookstoreservice.user.dto.address.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @Size(max = 50)
        @NotBlank
        String alias,// 별칭

        @Size(max = 255)
        @NotBlank(message = "도로명 주소는 필수 입력값입니다.")
        String roadAddress,//도로명 주소

        @Size(max = 10)
        @NotBlank(message = "우편번호는 필수 입력값입니다.")
        String zipCode, // 우편번호

        @Size(max = 255)
        String addressDetail
){
}
