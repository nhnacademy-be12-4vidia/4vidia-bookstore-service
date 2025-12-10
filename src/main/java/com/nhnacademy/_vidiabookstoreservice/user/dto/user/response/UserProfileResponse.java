package com.nhnacademy._vidiabookstoreservice.user.dto.user.response;


import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.dto.address.response.AddressResponse;

import java.time.LocalDate;

public record UserProfileResponse(
        Long userId,
        String email,
        String name,
        String phone,
        LocalDate birthDate,
        Integer point,
        AddressResponse defaultAddress,
        String gradeName

) {
    public static UserProfileResponse fromEntity(User user) {
        return new UserProfileResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getBirthDate(),
                user.getPoint(),
                user.getAddress() != null
                ? AddressResponse.fromEntity(user.getAddress()):null,
                user.getGrade().getGradeName().name()
        );
    }

}
