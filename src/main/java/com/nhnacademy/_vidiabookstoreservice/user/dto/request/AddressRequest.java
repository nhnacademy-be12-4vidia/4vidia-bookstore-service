package com.nhnacademy._vidiabookstoreservice.user.dto.request;

public record AddressRequest(
        String alias,// 별칭
        String roadAddress,//도로명 주소
        String zipCode, // 우편번호
        String addressDetail

){

}
