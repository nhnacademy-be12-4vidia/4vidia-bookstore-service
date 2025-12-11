package com.nhnacademy._vidiabookstoreservice.admin.controller;


import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminReviewPageResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.impl.AdminReviewServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/reviews")
public class AdminReviewController {
    private final AdminReviewServiceImpl adminReviewService;

    /**
     * 관리자 리뷰 목록 조회
     *
     */
    @GetMapping
    public ResponseEntity<AdminReviewPageResponse> getReviewPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        if (keyword != null && keyword.contains("%")) {
            keyword = URLDecoder.decode(keyword, StandardCharsets.UTF_8);
        }


        AdminReviewPageResponse response = adminReviewService.getReviews(keyword,rating ,page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * 관리자 리뷰 삭제
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        adminReviewService.deleteReview(reviewId);
        // 204 No Content 가 일반적인 삭제 응답
        return ResponseEntity.noContent().build();
    }


}
