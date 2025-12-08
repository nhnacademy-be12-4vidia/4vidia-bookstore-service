package com.nhnacademy._vidiabookstoreservice.user.dto.dormant.request;

// 인증 코드 전송 dto
public record DormantSendCodeRequest(
        String email,
        String webhookUrl
) {}