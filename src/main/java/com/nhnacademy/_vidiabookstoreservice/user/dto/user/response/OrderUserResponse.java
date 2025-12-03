package com.nhnacademy._vidiabookstoreservice.user.dto.user.response;


import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.dto.address.response.AddressResponse;

import java.util.List;

public record OrderUserResponse(
        String email,
        String name,
        String phone,
        int point, // todo : Integer로 변경해야함
        List<AddressResponse> addressResponses
) {
    public static OrderUserResponse fromEntity(User user) {
        return new OrderUserResponse(
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getPoint(),
                user.getAddresses().stream().map(AddressResponse::fromEntity).toList()
        );
    }
}
