package com.nhnacademy._vidiabookstoreservice.user.dto.address.request;

import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.Address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 주소 등록 DTO
public record CreateAddressRequest(
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
    public Address toEntity(User user) {
        return Address.builder()
                .user(user)
                .alias(this.alias)
                .roadAddress(this.roadAddress)
                .zipCode(this.zipCode)
                .addressDetail(this.addressDetail)
                .build();
    }
}
