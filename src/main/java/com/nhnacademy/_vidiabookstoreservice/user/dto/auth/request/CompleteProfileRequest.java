package com.nhnacademy._vidiabookstoreservice.user.dto.auth.request;

import java.time.LocalDate;

public record CompleteProfileRequest(
        String email,
        String name,
        String phone,
        LocalDate birthDate
) {
}