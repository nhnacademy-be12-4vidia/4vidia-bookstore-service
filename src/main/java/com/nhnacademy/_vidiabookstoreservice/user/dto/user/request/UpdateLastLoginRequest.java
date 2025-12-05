package com.nhnacademy._vidiabookstoreservice.user.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateLastLoginRequest(
        @NotBlank(message = "이메일은 필수 입력입니다.")
        @Size(max=50)
        String email
) {

}

