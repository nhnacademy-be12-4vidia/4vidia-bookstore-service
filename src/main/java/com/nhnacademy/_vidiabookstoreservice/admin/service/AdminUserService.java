package com.nhnacademy._vidiabookstoreservice.admin.service;

import com.nhnacademy._vidiabookstoreservice.admin.dto.AdminUserSearchCondition;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminUserResponse;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {
    Page<AdminUserResponse> getUsers(AdminUserSearchCondition condition, Pageable pageable);
    AdminUserResponse getUser(Long userId);
    void updateUserStatus(Long userId, UserStatus userStatus);
}
