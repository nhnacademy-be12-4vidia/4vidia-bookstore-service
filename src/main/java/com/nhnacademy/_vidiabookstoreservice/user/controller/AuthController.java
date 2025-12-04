package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.FindIdRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.FindPasswordRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.UserSignupRequest;
import com.nhnacademy._vidiabookstoreservice.user.service.AuthService;
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

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<Long> singup(@Valid @RequestBody UserSignupRequest userSignupRequest) {
        Long userId = authService.register(userSignupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userId); // 201 Created
    }

    /**
     * 회원 아이디(email) 찾기 -> 이름,생일,전화번호 조회해서 찾기
     * */
    @PostMapping("/find-id")
    public ResponseEntity<String> findUserId(@Valid @RequestBody FindIdRequest findIdRequest) {
        String email = authService.findUserId(findIdRequest);
        return ResponseEntity.ok().body(email); // 200 OK + JSON
    }


    /**
     * 회원 비밀번호 찾기 ->아이디,이름,전화번호 입력받아서 임시비밀번호 생성해서 -> 이메일로 보내기?
     */
    @PostMapping("/find-password")
    public ResponseEntity<String> findPassword(@Valid @RequestBody FindPasswordRequest findPasswordRequest) {
        authService.restPasswordAndSendMail(findPasswordRequest);
        return ResponseEntity.ok("임시 비밀번호가 발급되었습니다.");
    }

    @GetMapping("/check-email")
    public ResponseEntity<String> checkEmail(@RequestParam String email) {
        String decodedEmail = URLDecoder.decode(email, StandardCharsets.UTF_8);
        String string = authService.existsByEmail(decodedEmail).toString();
        return ResponseEntity.ok().body(string);
    }

}
