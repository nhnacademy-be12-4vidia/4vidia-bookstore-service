package com.nhnacademy._vidiabookstoreservice.book.dto.tag.response;

import com.nhnacademy._vidiabookstoreservice.book.domain.Tag;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TagResponse {

    private Long id;
    private String name;

    public static TagResponse from(Tag tag) {
        return TagResponse.builder()
            .id(tag.getId())
            .name(tag.getName())
            .build();
    }

}
