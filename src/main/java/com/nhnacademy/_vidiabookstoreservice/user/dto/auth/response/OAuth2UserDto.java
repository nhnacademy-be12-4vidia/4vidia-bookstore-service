package com.nhnacademy._vidiabookstoreservice.user.dto.auth.response;

import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuth2UserDto {
    private Long userId;
    private String email;
    private String role;
    private String status;

    public static OAuth2UserDto fromEntity(User user) {
        OAuth2UserDto oAuth2UserDto = new OAuth2UserDto();
        oAuth2UserDto.setUserId(user.getUserId());
        oAuth2UserDto.setEmail(user.getEmail());
        oAuth2UserDto.setRole(user.getRole().toString());
        oAuth2UserDto.setStatus(user.getStatus().toString());
        return oAuth2UserDto;
    }
}
