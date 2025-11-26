package com.nhnacademy._vidiabookstoreservice.user.dto.request;

// 로그인 요청
public record LoginRequest(
        String email,
        String password
) {
}
