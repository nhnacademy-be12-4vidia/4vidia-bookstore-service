package com.nhnacademy._vidiabookstoreservice.admin.service;

import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminReviewResponse;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;

public interface AdminReviewService {
    PageResponse<AdminReviewResponse> getReviews(
            String keyword,
            Integer rating,
            int page,
            int size
    );
    void deleteReview(Long reviewId);
}
