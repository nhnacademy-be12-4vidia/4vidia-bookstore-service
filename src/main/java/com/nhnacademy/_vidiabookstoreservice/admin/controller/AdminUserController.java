package com.nhnacademy._vidiabookstoreservice.admin.controller;


import com.nhnacademy._vidiabookstoreservice.admin.dto.AdminUserSearchCondition;
import com.nhnacademy._vidiabookstoreservice.admin.dto.request.UpdateUserStatusRequest;

import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminUserResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.AdminUserService;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {
    private final AdminUserService adminUserService;

    /**
     * 회원 목록 조회 (검색 + 페이징)
     *
     */

    @GetMapping
    public PageResponse<AdminUserResponse> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        // URL 인코딩 풀기
        if (keyword != null && keyword.contains("%")) {
            keyword = URLDecoder.decode(keyword, StandardCharsets.UTF_8);
        }
        // 검색 조건 생성
        AdminUserSearchCondition condition = new AdminUserSearchCondition(keyword, status);
        // 서비스 호출
        Page<AdminUserResponse> page = adminUserService.getUsers(condition, pageable);

        return PageResponse.from(page);
    }

    /**
     * 단일 회원 상세 조회
     */
    @GetMapping("/{userId}")
    public AdminUserResponse getUser(@PathVariable Long userId) {
        return adminUserService.getUser(userId);
    }

    /**
     * 회원 상태 변경 (ACTIVE / DORMANT / DELETED)
     */
    
    @PutMapping("/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UpdateUserStatusRequest request
    ) {
        adminUserService.updateUserStatus(userId, request.status());
        return ResponseEntity.noContent().build();
    }


}
