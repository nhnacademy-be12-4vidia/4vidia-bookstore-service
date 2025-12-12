package com.nhnacademy._vidiabookstoreservice.user.service;

import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.FindIdRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.FindPasswordRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.LoginRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.UserSignupRequest;

import java.time.LocalDateTime;

public interface AuthService {
    Long register(UserSignupRequest request);
    String findUserId(FindIdRequest request);
    String restPasswordAndSendMail (FindPasswordRequest request);
    Boolean existsByEmail(String email);
    Boolean isDormant(String email);
    int convertDormantUsers(LocalDateTime day);
}
