package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.user.dto.request.*;
import com.nhnacademy._vidiabookstoreservice.user.dto.response.UserProfileResponse;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/my") // todo /my/profile
@RestController
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<String> singup(@RequestBody @Valid UserSignupRequest userSignupRequest) {
        userService.register(userSignupRequest);
        return ResponseEntity.ok("성공적으로 저장되었습니다.");
    }

    /**
     * 회원정보 조회 (마이페이지)
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@RequestHeader("X-USER-ID") Long id) {
        UserProfileResponse user = userService.getUserInfo(id);
        return ResponseEntity.ok(user);
    }

    /**
     * 회원정보 수정
     */
    @PatchMapping("/profile")
    public ResponseEntity<String> updateUser(@RequestHeader("X-USER-ID") Long id,
                                           @RequestBody UpdateUserRequest updateUserRequest) {
        userService.updateUserInfo(id, updateUserRequest);
        return ResponseEntity.ok("수정 완료");
    }


    /**
     * 비밀번호 수정 => post가 맞데요
     */
    @PatchMapping("/change-password")
    public ResponseEntity<String> updatePassword(@RequestHeader("X-USER-ID") Long id,
                                                 @Valid @RequestBody ChangePasswordRequest changePasswordRequest, BindingResult bindingResult) {
        userService.changePassword(id, changePasswordRequest);
        return ResponseEntity.ok("비밀번호 수정 완료");
    }

    /**
     * 회원 탈퇴
     */
    @PatchMapping("/delete")
    public ResponseEntity<String> deleteUser(@RequestHeader("X-USER-ID") Long id,
                                             @RequestBody DeleteUserRequest deletePasswordRequest) {
        userService.deleteUserById(id, deletePasswordRequest);
        return ResponseEntity.ok("회원 탈퇴 완료");
    }


    /**
     * 회원 아이디 찾기 ???? -> 이름,생일,전화번호 조회해서 찾기
     * */
    @PostMapping("/find-id")
    public ResponseEntity<String> findId(@Valid @RequestBody FindIdRequest findIdRequest) {
        String email = userService.findUserId(findIdRequest);
        return ResponseEntity.ok(email);
    }


    /**
     * 회원 비밀번호 찾기 ->아이디,이름,전화번호 입력받아서  임시비밀번호 생성해서 -> 이메일로 보내기?
     */
//    @PostMapping("/find-password")
//    public ResponseEntity<String> findPassword(@Valid @RequestBody FindPasswordRequest findPasswordRequest) {
//        User user = userRepository.findByEmailAndNameAndPhone(
//                findPasswordRequest.
//        )
//    }







}
