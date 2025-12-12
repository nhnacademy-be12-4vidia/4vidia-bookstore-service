package com.nhnacademy._vidiabookstoreservice.user.dto.auth.response;

import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuth2UserDto {

    private String provider;
    private String socialId;
    private String name;
    private String email;
    private String phone;
    private String role;

    public static OAuth2UserDto fromEntity(User user) {
        OAuth2UserDto oAuth2UserDto = new OAuth2UserDto();
        oAuth2UserDto.setProvider(user.getProvider());
        oAuth2UserDto.setSocialId(user.getSocialId());
        oAuth2UserDto.setName(user.getName());
        oAuth2UserDto.setEmail(user.getEmail());
        oAuth2UserDto.setPhone(user.getPhone());
        oAuth2UserDto.setRole(user.getRole().toString());
        return oAuth2UserDto;
    }
}
