package com.nhnacademy._vidiabookstoreservice.user.dto.address.response;


import com.nhnacademy._vidiabookstoreservice.user.domain.Address;

public record AddressResponse(
    Long addressId,
    String alias,
    String roadAddress,
    String zipCode,
    String addressDetail
)
{

    /**
     *UserAddress 엔티티를 AddressResponse DTO로 변환하는 정적 팩토리 메서드
     */
    public static AddressResponse fromEntity(Address address) {
        return new AddressResponse(
                address.getAddressId(),
                address.getAlias(),
                address.getRoadAddress(),
                address.getZipCode(),
                address.getAddressDetail()
        );
    }
}
