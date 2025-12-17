package com.nhnacademy._vidiabookstoreservice.user.dto.user.response;


import com.nhnacademy._vidiabookstoreservice.user.domain.Address;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.dto.address.response.AddressResponse;

import java.util.List;

public record OrderUserResponse(
        String email,
        String name,
        String phone,
        Integer point,
        Long addressId,
        String alias,
        String roadAddress,
        String zipCode,
        String addressDetail,
        List<AddressResponse> addressResponses
) {
    public static OrderUserResponse fromEntity(User user, Address defaultAddress) {
        Long addressId = null;
        String alias = null;
        String roadAddress = null;
        String zipCode = null;
        String addressDetail = null;
        if (defaultAddress != null) {
            addressId = defaultAddress.getAddressId();
            alias = defaultAddress.getAlias();
            roadAddress = defaultAddress.getRoadAddress();
            zipCode = defaultAddress.getZipCode();
            addressDetail = defaultAddress.getAddressDetail();
        }
        return new OrderUserResponse(
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getPoint(),
                addressId,
                alias,
                roadAddress,
                zipCode,
                addressDetail,
                user.getAddresses().stream().map(AddressResponse::fromEntity).toList()
        );
    }
}
