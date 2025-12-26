package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.like.response.LikeResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users/me/likes")
public class LikeController {
    private final LikeService likeService;

    /**
     * 좋아요 리스트 조회(my page에서 사용)
     */
    @GetMapping
    public ResponseEntity<PageResponse<LikeResponse>> getLikeListPage(@PageableDefault(size = 10) Pageable pageable){
        Long userId = UserContext.get().getUserId();

        return ResponseEntity.ok().body(likeService.getLikesPage(userId, pageable));
    }

    /**
     * 좋아요 리스트 조회
     */
    @GetMapping("/all")
    public ResponseEntity<List<LikeResponse>> getLikeList(){
        Long userId = UserContext.get().getUserId();

        return ResponseEntity.ok().body(likeService.getLikes(userId)); // 200 OK + JSON
    }

    /**
     * 좋아요 등록
     */
    @PostMapping("/{book-id}")
    public ResponseEntity<Void> addLike(@PathVariable("book-id") Long bookId) {
        if (UserContext.get() == null || UserContext.get().getUserId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 반환
        }

        Long userId = UserContext.get().getUserId();

        likeService.addLike(userId, bookId);
        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 Created
    }

    /**
     * 좋아요 삭제
     */
    @DeleteMapping("/{book-id}")
    public ResponseEntity<Void> removeLike(@PathVariable("book-id") Long bookId) {
        if (UserContext.get() == null || UserContext.get().getUserId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 반환
        }

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
