package com.nhnacademy._vidiabookstoreservice.admin.service.impl;


import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminReviewPageResponse;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminReviewResponse;
import com.nhnacademy._vidiabookstoreservice.book.domain.Review;
import com.nhnacademy._vidiabookstoreservice.book.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminReviewServiceImpl {
    private final ReviewRepository reviewRepository;

    /**
     * 관리자 리뷰 목록 조회 (검색 + 페이징)
     */
    public AdminReviewPageResponse getReviews(String keyword,Integer rating, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Review> reviewPage = reviewRepository.searchAdminReviews(
                (keyword == null || keyword.isBlank()) ? null : keyword.trim(),
                rating,
                pageable
        );

        return new AdminReviewPageResponse(
                reviewPage.getContent().stream()
                        .map(AdminReviewResponse::from)
                        .toList(),
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalPages(),
                reviewPage.getTotalElements(),
                reviewPage.isFirst(),
                reviewPage.isLast()
        );
    }
        /**
         * 리뷰 삭제
         */
        public void deleteReview(Long reviewId){
            if(!reviewRepository.existsById(reviewId)) {
                throw new IllegalArgumentException("리뷰를 찾을 수 없습니다. id="+reviewId);
            }
            reviewRepository.deleteById(reviewId);
        }

    }

