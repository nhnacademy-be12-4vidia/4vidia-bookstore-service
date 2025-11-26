package com.nhnacademy._vidiabookstoreservice.user.dto.request;

// 주소 등록 DTO
public record CreateAddressRequest(
        String alias,// 별칭
        String roadAddress,//도로명 주소
        String zipCode, // 우편번호
        String addressDetail
){

}
