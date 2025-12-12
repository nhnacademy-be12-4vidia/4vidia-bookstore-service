package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.user.dto.auth.response.OAuth2UserDto;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.ChangePasswordRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.DeleteUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.UpdateUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.UserInfoResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.UserProfileResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * 회원 이름 조회
     */
    @GetMapping("/name")
    public ResponseEntity<String> getUserName(@RequestHeader("X-User-Id") Long userId) {
        String userName = userService.getUserName(userId);
        return ResponseEntity.ok().body(userName);
    }

    /**
     * 회원정보 조회 (마이페이지)
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@RequestHeader("X-User-Id") Long id) {
        UserProfileResponse user = userService.getUserInfo(id);
        return ResponseEntity.ok().body(user); // 200 OK + JSON
    }

    /**
     * 회원정보 수정
     */
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateUserProfile(@RequestHeader("X-User-Id") Long id,
                                                                 @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        UserProfileResponse userProfileResponse = userService.updateUserProfile(id, updateUserRequest);
        return ResponseEntity.ok().body(userProfileResponse); // 204 No Content ? 200 OK + JSON ??
    }

    /**
     * 비밀번호 수정
     */
    @PutMapping("/me/password") // 기존 "/change-password"
    public ResponseEntity<Void> changePassword(@RequestHeader("X-User-Id") Long id,
                                               @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {

        userService.changePassword(id, changePasswordRequest);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * 회원 탈퇴
     */
    @PutMapping("/delete")
    public ResponseEntity<Void> deleteUser(@RequestHeader("X-User-Id") Long id,
                                           @Valid @RequestBody DeleteUserRequest deletePasswordRequest) {
        userService.deleteUserById(id, deletePasswordRequest);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * 회원 역할 조회
     */
    @GetMapping("/role")
    public ResponseEntity<String> getUserRole(@RequestHeader("X-User-Id") Long userId) {
        String userRole = userService.getUserRole(userId);
        return ResponseEntity.ok().body(userRole);
    }

    // 기존 회원 아이디/비밀번호 찾기 auth controller 에 있어서(중복) 삭제함

    /**
     * OAuth2용 유저 찾기
     */
    @GetMapping("/oauth2")
    public ResponseEntity<OAuth2UserDto> getOAuth2User(@RequestParam String provider, @RequestParam String socialId) {
        OAuth2UserDto oAuth2User = userService.getOAuth2User(provider, socialId);
        return ResponseEntity.ok().body(oAuth2User);
    }
}
