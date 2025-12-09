package com.nhnacademy._vidiabookstoreservice.admin.dto.request;

import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;

public record UpdateUserStatusRequest(
        UserStatus status
) {
}
