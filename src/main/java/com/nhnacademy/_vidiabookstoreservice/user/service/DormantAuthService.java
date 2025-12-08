package com.nhnacademy._vidiabookstoreservice.user.service;

public interface DormantAuthService {
    void sendAuthCode(String email, String webhookUrl);
    void verifyAuthCode(String email, String code);
    void sendAuthCodeByEmail(String email, String sendToEmail);

}
