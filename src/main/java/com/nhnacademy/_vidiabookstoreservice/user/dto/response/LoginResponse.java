package com.nhnacademy._vidiabookstoreservice.user.dto.response;


import com.nhnacademy._vidiabookstoreservice.user.domain.User;

// 로그인 응답
public record LoginResponse(
        String email,
        String password // password는 넘어가면 안되는데..
) {
    public static LoginResponse from(User user){
       return new LoginResponse(
               user.getEmail(),
               user.getPassword()
       );
    }
}
