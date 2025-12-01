package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.user.dto.request.*;
import com.nhnacademy._vidiabookstoreservice.user.dto.response.UserProfileResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/my")
@RestController
public class UserController {

    private final UserService userService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<Void> singup(@Valid @RequestBody UserSignupRequest userSignupRequest) {
        userService.register(userSignupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 Created
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
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestHeader("X-User-Id") Long id,
                                               @Valid @RequestBody ChangePasswordRequest changePasswordRequest, BindingResult bindingResult) {
        userService.changePassword(id, changePasswordRequest);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * 회원 탈퇴
     */
    @PutMapping("/delete") // todo : 실제로 회원을 삭제하지 않는데 맵핑, 메서드명에 delete가 맞는가?
    public ResponseEntity<Void> deleteUser(@RequestHeader("X-User-Id") Long id,
                                           @Valid @RequestBody DeleteUserRequest deletePasswordRequest) {
        userService.deleteUserById(id, deletePasswordRequest);
        return ResponseEntity.noContent().build(); // 204 No Content
    }


    /**
     * 회원 아이디(email) 찾기 -> 이름,생일,전화번호 조회해서 찾기
     * */
    @PostMapping("/find-id")
    public ResponseEntity<String> findUserId(@Valid @RequestBody FindIdRequest findIdRequest) {
        String email = userService.findUserId(findIdRequest);
        return ResponseEntity.ok().body(email); // 200 OK + JSON
    }


    /**
     * 회원 비밀번호 찾기 ->아이디,이름,전화번호 입력받아서 임시비밀번호 생성해서 -> 이메일로 보내기?
     */
    @PostMapping("/find-password")
    public ResponseEntity<String> findPassword(@Valid @RequestBody FindPasswordRequest findPasswordRequest) {
        userService.restPasswordAndSendMail(findPasswordRequest);
        return ResponseEntity.ok("임시 비밀번호가 발급되었습니다.");
    }







}
