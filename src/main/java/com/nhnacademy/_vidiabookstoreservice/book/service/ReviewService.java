package com.nhnacademy._vidiabookstoreservice.book.service;

import com.nhnacademy._vidiabookstoreservice.book.domain.Review;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.request.ReviewCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.request.ReviewUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.response.ReviewListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {

    Page<ReviewListResponse> getReviewListByBookId(Long bookId, Long userId, Pageable pageable);

    void createReview(ReviewCreateRequest request, Long userId, List<MultipartFile> reviewImageList);

    List<Long> getReviewedOrderItemIdList(List<Long> orderItemIdList);

    Long getTotalReviewCount(Long bookId);

    Double getAvgReviewRating(Long bookId);

    List<Review> getTop200ReviewListByBookId(Long bookId);

    void deactivateReview(Long userId, Long reviewId, Long bookId);

    void updateReview(Long userId, Long reviewId, Long bookId, ReviewUpdateRequest request, List<MultipartFile> newImageList);

}
