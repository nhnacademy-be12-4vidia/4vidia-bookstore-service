package com.nhnacademy._vidiabookstoreservice.admin.controller;



import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminReviewResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.impl.AdminReviewServiceImpl;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
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
     * 관리자 리뷰 목록 조회 (검색 + 평점 + 페이징)
     */
    @GetMapping
    public PageResponse<AdminReviewResponse> getReviewPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // URL 인코딩 풀기 (한글 검색 대비)
        if (keyword != null && keyword.contains("%")) {
            keyword = URLDecoder.decode(keyword, StandardCharsets.UTF_8);
        }

        // 서비스에서 PageResponse 만들어서 리턴하게 함
        return adminReviewService.getReviews(keyword, rating, page, size);
    }

    /**
     * 관리자 리뷰 삭제
     */
    @DeleteMapping("/{reviewId}")
    public void deleteReview(@PathVariable Long reviewId) {
        adminReviewService.deleteReview(reviewId);
        // 204 No Content 가 일반적인 삭제 응답
    }


}
