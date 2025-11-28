package com.nhnacademy._vidiabookstoreservice.user.dto.response;


import com.nhnacademy._vidiabookstoreservice.user.domain.UserAddress;

public record AddressResponse(
    Long userAddressId,
    String alias,
    String roadAddress,
    String zipCode,
    String addressDetail
)
{

    /**
     *UserAddress 엔티티를 AddressResponse DTO로 변환하는 정적 팩토리 메서드
     */
    public static AddressResponse fromEntity(UserAddress userAddress) {
        return new AddressResponse(
                userAddress.getUserAddressId(),
                userAddress.getAlias(),
                userAddress.getRoadAddress(),
                userAddress.getPostalAddress(),
                userAddress.getAddressDetail()
        );
    }
}
