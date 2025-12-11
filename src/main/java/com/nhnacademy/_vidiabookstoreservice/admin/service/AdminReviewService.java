package com.nhnacademy._vidiabookstoreservice.admin.service;

import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminReviewPageResponse;

public interface AdminReviewService {
    AdminReviewPageResponse getReviews(String keyword, Integer rating, int page, int size);
    void deleteReview(Long reviewId);
}
