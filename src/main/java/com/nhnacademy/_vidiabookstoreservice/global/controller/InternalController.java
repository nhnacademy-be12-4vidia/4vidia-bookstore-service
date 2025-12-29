package com.nhnacademy._vidiabookstoreservice.global.controller;

import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.PaycoUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.response.AuthUserDto;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.response.OAuth2UserDto;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.UserInfoResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.AuthService;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class InternalController {
    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/internal/users")
    public ResponseEntity<AuthUserDto> getInternalUserByEmail(@RequestParam String email) {
        AuthUserDto user = userService.getAuthUser(email);
        log.info("{}",user.email());
        return ResponseEntity.ok().body(user); // 200 OK + JSON
    }

    /**
     * 페이코 계정 찾거나 만들기(auth서버에서 사용)
     */
    @PostMapping("/internal/payco/find-or-create")
    public ResponseEntity<OAuth2UserDto> findOrCreateByPaycoId(@RequestBody PaycoUserRequest paycoUserRequest) {
        OAuth2UserDto payco = authService.findOrCreateOAuthUser("payco", paycoUserRequest);
        return ResponseEntity.ok().body(payco);
    }

    @GetMapping("/internal/users/{userId}/exists")
    public ResponseEntity<Void> exists(@PathVariable Long userId) {
        userService.getUserById(userId); // 없으면 예외
        return ResponseEntity.ok().build();
    }
}
