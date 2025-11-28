package com.nhnacademy._vidiabookstoreservice.user.service;

import com.nhnacademy._vidiabookstoreservice.user.dto.request.*;
import com.nhnacademy._vidiabookstoreservice.user.dto.response.UserProfileResponse;

public interface UserService {
    Long register(UserSignupRequest request);
    UserProfileResponse getUserInfo(Long id);
    void updateUserInfo(Long id, UpdateUserRequest request);
    void changePassword(Long id, ChangePasswordRequest request);
    void deleteUserById(Long id, DeleteUserRequest request);
    String findUserId(FindIdRequest request);
}
