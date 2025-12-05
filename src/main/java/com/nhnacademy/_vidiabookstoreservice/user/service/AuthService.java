package com.nhnacademy._vidiabookstoreservice.user.service;

import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.FindIdRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.FindPasswordRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.UserSignupRequest;

public interface AuthService {
    Long register(UserSignupRequest request);
    String findUserId(FindIdRequest request);
    String restPasswordAndSendMail (FindPasswordRequest request);
    Boolean existsByEmail(String email);
    String getUserStatus(String email);
}
