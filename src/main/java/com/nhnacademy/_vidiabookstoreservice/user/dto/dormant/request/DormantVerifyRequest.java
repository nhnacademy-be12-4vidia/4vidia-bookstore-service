package com.nhnacademy._vidiabookstoreservice.user.dto.dormant.request;

// 인증 코드 검증 dto
public record DormantVerifyRequest(
        String email,
        String code
) {}