package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.user.dto.response.LikeResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/my/likes")
public class LikeController {
    private final LikeService likeService;

    /**
     * 좋아요 리스트 조회
     */
    @GetMapping
    public ResponseEntity<List<LikeResponse>> getLikeList(@RequestHeader("X-User-Id") Long userId){
        return ResponseEntity.ok().body(likeService.getLikes(userId)); // 200 OK + JSON
    }

    /**
     * 좋아요 등록
     */
    @PostMapping("/{bookId}")
    public ResponseEntity<Void> addLike(@RequestHeader("X-User-Id") Long userId,
                                          @PathVariable Long bookId) {
        likeService.addLike(userId, bookId);
        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 Created
    }

    /**
     * 좋아요 삭제
     */
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> removeLike(@RequestHeader("X-User-Id") Long userId,
                                             @PathVariable Long bookId) {
        likeService.removeLike(userId, bookId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
