package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.global.dto.ApiResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.FindIdRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.FindPasswordRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.PaycoUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.response.OAuth2UserDto;
import com.nhnacademy._vidiabookstoreservice.user.dto.dormant.request.DormantSendCodeByEmailRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.dormant.request.DormantSendCodeRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.dormant.request.DormantVerifyRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.UpdateLastLoginRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.UserSignupRequest;
import com.nhnacademy._vidiabookstoreservice.user.service.AuthService;
import com.nhnacademy._vidiabookstoreservice.user.service.DormantAuthService;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final DormantAuthService dormantAuthService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@Valid @RequestBody UserSignupRequest userSignupRequest) {
        Long userId = authService.register(userSignupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userId); // 201 Created
    }


    /**
     * 회원 아이디(email) 찾기
     * */
    @PostMapping("/find-id")
    public ApiResponse<String> findUserId(@Valid @RequestBody FindIdRequest findIdRequest) {
        String email = authService.findUserId(findIdRequest);
        return ApiResponse.success(email);
    }

    /**
     * 회원 비밀번호 새로 발급
     * 기존 "/find-password"
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Void> findPassword(@Valid @RequestBody FindPasswordRequest findPasswordRequest) {
        authService.restPasswordAndSendMail(findPasswordRequest);
        return ResponseEntity.ok().build();
    }

    /**
     * 이메일 중복여부
     * 기존 "/check-email"
     */
    @GetMapping("/emails/exists")
    @ResponseBody
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        String decodedEmail = URLDecoder.decode(email, StandardCharsets.UTF_8);
        Boolean existsByEmail = authService.existsByEmail(decodedEmail);
        return ResponseEntity.ok().body(existsByEmail);
    }

    /**
     * 로그인 후 -> 휴먼 여부 확인
     * 기존 "/check-dormant"
     */
    @GetMapping("/dormant")
    public ResponseEntity<Boolean> checkDormant(@RequestParam String email) {
        return ResponseEntity.ok().body(authService.isDormant(email));
    }

    /**
     * 휴면 인증코드 전송
     */
    @PostMapping("/dormant/send-code")
    public ResponseEntity<Void> send(@RequestBody DormantSendCodeRequest req){
        dormantAuthService.sendAuthCode(req.email(),req.webhookUrl());
        return ResponseEntity.ok().build();
    }

    /**
     * 휴면 인증코드 검증
     */
    @PostMapping("/dormant/verify")
    public ResponseEntity<Void> verify(@RequestBody DormantVerifyRequest req){
        String email = URLDecoder.decode(req.email(), StandardCharsets.UTF_8);
        dormantAuthService.verifyAuthCode(email, req.code());
        return ResponseEntity.ok().build();
    }

    /**
     * 메일로 휴면 인증코드 전송
     */
    @PostMapping("/dormant/send-code/email")
    public ResponseEntity<Void> sendCodeByEmail(@RequestBody DormantSendCodeByEmailRequest req) {
        dormantAuthService.sendAuthCodeByEmail(req.email(), req.contactEmail());
        return ResponseEntity.ok().build();
    }

    /**
     * 마지막로그인시간 업데이트하기
     * 기존 "/update-time"
     */
    @PutMapping("/last-login")
    public void updateLastLoginAt(@RequestBody UpdateLastLoginRequest updateLastLoginRequest) {
        userService.updateLastLoginAt(updateLastLoginRequest.email());
    }


}
