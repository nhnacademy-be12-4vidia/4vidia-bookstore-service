package com.nhnacademy._vidiabookstoreservice.user.dto.user.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 비밀번호 변경 dto
 */
public record ChangePasswordRequest (
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,20}$",
                message = "비밀번호는 소문자, 숫자, 특수문자를 포함한 8~20자여야 합니다."
        )
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력해주세요. ")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,20}$",
                message = "비밀번호는 소문자, 숫자, 특수문자를 포함한 8~20자여야 합니다."
        )
        String newPassword,

        @NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,20}$",
                message = "비밀번호는 소문자, 숫자, 특수문자를 포함한 8~20자여야 합니다."
        )
        String confirmPassword
){

}
