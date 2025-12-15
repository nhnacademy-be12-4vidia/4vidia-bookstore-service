package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.user.dto.like.response.LikeResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.LikeService;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users/me/likes") // 기존 "/my/likes"
public class LikeController {
    private final LikeService likeService;

    /**
     * 좋아요 리스트 조회
     */
    @GetMapping
    public ResponseEntity<List<LikeResponse>> getLikeList(){
        Long userId = UserContext.get().getUserId();

        return ResponseEntity.ok().body(likeService.getLikes(userId)); // 200 OK + JSON
    }

    /**
     * 좋아요 등록
     */
    @PostMapping("/{book-id}")
    public ResponseEntity<Void> addLike(@PathVariable("book-id") Long bookId) {
        Long userId = UserContext.get().getUserId();

        likeService.addLike(userId, bookId);
        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 Created
    }

    /**
     * 좋아요 삭제
     */
    @DeleteMapping("/{book-id}")
    public ResponseEntity<Void> removeLike(@PathVariable("book-id") Long bookId) {
        Long userId = UserContext.get().getUserId();

        likeService.removeLike(userId, bookId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * 좋아요 전체 삭제
     */
    @DeleteMapping
    public ResponseEntity<Void> removeAllLike(){
        Long userId = UserContext.get().getUserId();
        List<Long> bookIds = likeService.getLikes(userId).stream()
                .map(LikeResponse::bookId)
                .toList();
        likeService.removeAllLike(userId, bookIds);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
