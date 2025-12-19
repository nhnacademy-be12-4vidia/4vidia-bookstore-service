package com.nhnacademy._vidiabookstoreservice.admin.service.impl;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminReviewResponse;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.ReviewNotFoundException;
import com.nhnacademy._vidiabookstoreservice.admin.service.AdminReviewService;
import com.nhnacademy._vidiabookstoreservice.book.domain.Review;
import com.nhnacademy._vidiabookstoreservice.book.repository.ReviewRepository;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminReviewServiceImpl implements AdminReviewService {
    private final ReviewRepository reviewRepository;

    /**
     * 관리자 리뷰 목록 조회 (검색 + 평점 + 페이징)
     */
    @Override
    public PageResponse<AdminReviewResponse> getReviews(
            String keyword,
            Integer rating,
            int page,
            int size
    ) {
        // 검색어 정리 (null / 공백 → null)
        String trimmed = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")   // 최신 리뷰 먼저
        );

        // 엔티티 페이지 조회
        Page<Review> reviewPage =
                reviewRepository.searchAdminReviews(trimmed, rating, pageable);

        // 엔티티 → DTO 페이지로 매핑
        Page<AdminReviewResponse> dtoPage =
                reviewPage.map(AdminReviewResponse::from);

        // 공통 PageResponse로 감싸서 리턴
        return PageResponse.from(dtoPage);
    }


        /**
         * 리뷰 삭제
         */
        public void deleteReview(Long reviewId){
            if(!reviewRepository.existsById(reviewId)) {
                throw new ReviewNotFoundException(reviewId);
            }
            reviewRepository.deleteById(reviewId);
        }

    }

