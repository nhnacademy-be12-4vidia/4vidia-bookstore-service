package com.nhnacademy._vidiabookstoreservice.admin.service.impl;

import com.nhnacademy._vidiabookstoreservice.admin.dto.AdminUserSearchCondition;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminUserResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.AdminUserService;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserServiceImpl implements AdminUserService {
    private final UserRepository userRepository;

    @Override
    public Page<AdminUserResponse> getUsers(AdminUserSearchCondition condition, Pageable pageable) {
        String keyword = condition.keyword();
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) {
                keyword = null;
            }
        }
        UserStatus status = condition.status();

        Page<User> users = userRepository.searchAdminUsers(status,keyword,pageable);
        log.info("검색 keyword = [{}]", keyword);

        return users.map(AdminUserResponse::from);
    }

    @Override
    public AdminUserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return AdminUserResponse.from(user);
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, UserStatus status){
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        user.setStatus(status);
    }

}
