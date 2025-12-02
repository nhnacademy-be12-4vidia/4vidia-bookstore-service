package com.nhnacademy._vidiabookstoreservice.book.dto.tag.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagCreateRequest {
    @NotBlank(message = "태그 이름은 필수입니다.")
    private String name;

}
