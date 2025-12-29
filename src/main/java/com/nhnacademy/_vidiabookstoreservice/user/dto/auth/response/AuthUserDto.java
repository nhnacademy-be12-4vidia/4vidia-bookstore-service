package com.nhnacademy._vidiabookstoreservice.user.dto.auth.response;

import com.nhnacademy._vidiabookstoreservice.user.domain.User;

public record AuthUserDto(
        Long id,
        String email,
        String password,
        String roles,        // "ROLE_USER", "ROLE_ADMIN"
        String status
) {
    public static AuthUserDto fromEntity(User user) {
    return new AuthUserDto(
            user.getUserId(),
            user.getEmail(),
            user.getPassword(),
            user.getRole().name(),
            user.getStatus().toString()
    );
}
}
