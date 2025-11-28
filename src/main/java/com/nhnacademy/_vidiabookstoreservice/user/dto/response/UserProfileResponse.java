package com.nhnacademy._vidiabookstoreservice.user.dto.response;


import com.nhnacademy._vidiabookstoreservice.user.domain.User;

import java.time.LocalDate;

public record UserProfileResponse(
        Long userId,
        String email,
        String name,
        String phone,
        LocalDate birthDate,
        int point,
        AddressResponse defaultAddress,
        String gradeName

) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getBirthDate(),
                user.getPoint(),
                user.getUserAddress() != null
                ? AddressResponse.fromEntity(user.getUserAddress()):null,
                user.getGrade().getGradeName().name()
        );
    }

}
