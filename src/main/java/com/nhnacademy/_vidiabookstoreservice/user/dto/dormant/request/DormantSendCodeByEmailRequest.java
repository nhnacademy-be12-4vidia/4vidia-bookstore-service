package com.nhnacademy._vidiabookstoreservice.user.dto.dormant.request;

public record DormantSendCodeByEmailRequest(
        String email,
        String contactEmail
) {}