package com.nhnacademy._vidiabookstoreservice.user.dto.user.response;

import com.nhnacademy._vidiabookstoreservice.user.domain.User;

// user-service -> auth
public record UserInfoResponse(
        Long id,
        String email,
        String password,
        String roles        // "ROLE_USER", "ROLE_ADMIN"
) {
    public static UserInfoResponse fromEntity(User user) {
        return new UserInfoResponse(
                user.getUserId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().name()
        );
    }
}