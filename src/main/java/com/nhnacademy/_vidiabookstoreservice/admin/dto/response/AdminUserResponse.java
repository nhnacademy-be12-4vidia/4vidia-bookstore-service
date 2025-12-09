package com.nhnacademy._vidiabookstoreservice.admin.dto.response;

import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminUserResponse(
        Long userId,
        String email,
        String name,
        String phone,
        LocalDate birthDate,
        Integer point,
        UserStatus status,
        LocalDate joinedAt,
        LocalDateTime lastLoginAt,
        String gradeName
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getBirthDate(),
                user.getPoint(),
                user.getStatus(),
                user.getCreatedAt(),     // BaseEntity 의 createdAt (대부분 LocalDateTime일 것)
                user.getLastLoginAt(),
                user.getGrade() != null ? user.getGrade().getGradeName().name() : null
        );
    }
}
