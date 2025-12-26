package com.nhnacademy._vidiabookstoreservice.user.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailSendCodeRequest(
        @Email
        @NotBlank
        String email
) {
}
