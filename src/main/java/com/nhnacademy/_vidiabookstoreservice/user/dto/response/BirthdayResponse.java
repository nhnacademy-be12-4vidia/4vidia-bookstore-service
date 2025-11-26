package com.nhnacademy._vidiabookstoreservice.user.dto.response;

import java.time.LocalDate;

public record BirthdayResponse (
        Long userId,
        LocalDate birthDate
){
}
