package com.nhnacademy._vidiabookstoreservice.admin.dto;

import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;

public record AdminUserSearchCondition(
        String keyword,
        UserStatus status
) {
}
