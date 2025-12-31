package com.nhnacademy._vidiabookstoreservice.user.service;

import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.like.response.LikeResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.like.response.UserLikeResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LikeService {

    PageResponse<LikeResponse> getLikesPage(Long userId, Pageable pageable);
    List<LikeResponse> getLikes(Long userId);

    List<UserLikeResponse> getLikeIdList(Long userId, List<Long> bookIds);
    void addLike(Long userId, Long bookId);
    void removeLike(Long userId, Long bookId);
    void removeAllLike(Long userId, List<Long> bookIds);
}
