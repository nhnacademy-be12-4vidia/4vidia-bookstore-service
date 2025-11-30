package com.nhnacademy._vidiabookstoreservice.user.dto.request;


import jakarta.validation.constraints.NotBlank;

/**
 * 비밀번호 변경 dto
 */
public record ChangePasswordRequest (
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력해주세요. ")
        String newPassword,

        @NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
        String confirmPassword
){

}
