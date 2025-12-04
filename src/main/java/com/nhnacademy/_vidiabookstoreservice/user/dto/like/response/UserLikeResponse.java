package com.nhnacademy._vidiabookstoreservice.user.dto.like.response;

import com.nhnacademy._vidiabookstoreservice.user.domain.Like;

public record UserLikeResponse(
        Long likeId
) {
    public static UserLikeResponse fromEntity(Like like) {
        return new UserLikeResponse(
                like.getLikeId()
        );
    }
}
