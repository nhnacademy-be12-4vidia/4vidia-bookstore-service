package com.nhnacademy._vidiabookstoreservice.user.dto.auth.request;

import jakarta.validation.constraints.NotBlank;

public record EmailVerifyCodeRequest(
        @NotBlank
        String email,
        @NotBlank
        String code
) {
}
