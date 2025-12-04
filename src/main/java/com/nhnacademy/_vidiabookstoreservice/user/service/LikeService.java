package com.nhnacademy._vidiabookstoreservice.user.service;

import com.nhnacademy._vidiabookstoreservice.user.dto.like.response.LikeResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.like.response.UserLikeResponse;

import java.util.List;

public interface LikeService {
    boolean isLiked(Long userId, Long bookId);
    List<LikeResponse> getLikes(Long userId);
    List<UserLikeResponse> getLikeIdList(Long userId, List<Long> bookIds);
    void addLike(Long userId, Long bookId);
    void removeLike(Long userId, Long bookId);
}
