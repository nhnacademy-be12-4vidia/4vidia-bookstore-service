package com.nhnacademy._vidiabookstoreservice.user.service;

import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.FindIdRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.FindPasswordRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.response.OAuth2UserDto;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.ChangePasswordRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.DeleteUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.UpdateUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.OrderUserResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.UserInfoResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.UserProfileResponse;


public interface UserService {
    UserInfoResponse getUserByEmail(String email);
    User getProxyById(Long userId);
    User getUserById(Long userId);
    Integer getUserByPoint(Long userId);

    String getUserName(Long userId);
    UserProfileResponse getUserInfo(Long id);
    UserProfileResponse updateUserProfile(Long id, UpdateUserRequest request);
    OrderUserResponse getOrderUser(Long userId);
    void changePassword(Long id, ChangePasswordRequest request);
    void deleteUserById(Long id, DeleteUserRequest request);

    String findUserId(FindIdRequest request);
    String restPasswordAndSendMail (FindPasswordRequest request);

    void updateLastLoginAt(String email);

    String getUserRole(Long userId);

    OAuth2UserDto getOAuth2User(String provider, String socialId);
}
