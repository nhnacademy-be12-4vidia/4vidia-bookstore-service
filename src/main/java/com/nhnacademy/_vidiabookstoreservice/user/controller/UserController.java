package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.global.dto.ApiResponse;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.CompleteProfileRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.ChangePasswordRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.DeleteUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.UpdateUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.OrderUserResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.UserInfoResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.UserProfileResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users") // 기존 "/my"
@RestController
public class UserController {

    private final UserService userService;

    /**
     * 이메일로 회원 조회
     */
    @GetMapping // 기존 "/users?email=.."
    public ResponseEntity<UserInfoResponse> getUserByEmail(@RequestParam String email) {
        UserInfoResponse user = userService.getUserByEmail(email);
        return ResponseEntity.ok().body(user); // 200 OK + JSON
    }

    /**
     * 아이디로 회원 조회
     */
    @GetMapping("/id")
    public ResponseEntity<UserProfileResponse> getUserById() {
        Long userId = UserContext.get().getUserId();
        User user = userService.getUserById(userId);
        return ResponseEntity.ok().body(UserProfileResponse.fromEntity(user));
    }

    /**
     * userId 받는게 필요함..
     * @param userId
     */
    @GetMapping("/{userId}/exists")
    public ResponseEntity<Void> exists(@PathVariable Long userId) {
        userService.getUserById(userId); // 없으면 예외
        return ResponseEntity.ok().build();
    }


    /**
     * 회원 이름 조회
     */
    @GetMapping("/name")
    public ApiResponse<String> getUserName() {
        Long userId = UserContext.get().getUserId();

        String userName = userService.getUserName(userId);
        return ApiResponse.success(userName);
    }

    /**
     * 회원정보 조회 (마이페이지)
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile() {
        Long userId = UserContext.get().getUserId();

        UserProfileResponse user = userService.getUserInfo(userId);
        return ResponseEntity.ok().body(user); // 200 OK + JSON
    }

    /**
     * 회원정보 수정
     */
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateUserProfile(@Valid @RequestBody UpdateUserRequest updateUserRequest) {
        Long userId = UserContext.get().getUserId();

        UserProfileResponse userProfileResponse = userService.updateUserProfile(userId, updateUserRequest);
        return ResponseEntity.ok().body(userProfileResponse); // 204 No Content ? 200 OK + JSON ??
    }

    /**
     * 비밀번호 수정
     */
    @PutMapping("/me/password") // 기존 "/change-password"
    public void changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        Long userId = UserContext.get().getUserId();

        userService.changePassword(userId, changePasswordRequest);
    }

    /**
     * 회원 탈퇴
     */
    @PutMapping("/delete")
    public void deleteUser(@Valid @RequestBody DeleteUserRequest deletePasswordRequest) {
        Long userId = UserContext.get().getUserId();
        userService.deleteUserById(userId, deletePasswordRequest);
    }

    /**
     * 회원 역할 조회
     */
    @GetMapping("/role")
    public ApiResponse<String> getUserRole() {
        Long userId = UserContext.get().getUserId();
        String userRole = userService.getUserRole(userId);
        return ApiResponse.success(userRole);
    }

    // 기존 회원 아이디/비밀번호 찾기 auth controller 에 있어서(중복) 삭제함

    /**
     * payco 로그인 전용 필수 정보 입력
     */
    @PutMapping("/complete-profile")
    public ResponseEntity<Void> updateCompleteProfile(@RequestBody CompleteProfileRequest completeProfileRequest) {
        Long userId = UserContext.get().getUserId();
        userService.completeProfile(userId, completeProfileRequest);
        return ResponseEntity.ok().build();
    }

}
