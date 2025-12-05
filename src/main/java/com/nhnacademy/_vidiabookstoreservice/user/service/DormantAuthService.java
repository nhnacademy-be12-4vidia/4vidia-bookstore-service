package com.nhnacademy._vidiabookstoreservice.user.service;

public interface DormantAuthService {
    void sendDormantCode(String email);
    boolean verifyAuthCode(String email, String inputCode);
    void activateUser(String email);
}
