package com.nhnacademy._vidiabookstoreservice.user.dto.user.request;


import jakarta.validation.constraints.NotBlank;

/**
 * 회원 탈퇴 시 비밀번호 확인 dto
 */
public record DeleteUserRequest( 
    @NotBlank(message = "비밀번호를 입력해주세요.")
    String currentPassword
)
{

}