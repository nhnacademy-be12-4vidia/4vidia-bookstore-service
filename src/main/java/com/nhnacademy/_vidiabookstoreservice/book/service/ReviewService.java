package com.nhnacademy._vidiabookstoreservice.book.service;

import com.nhnacademy._vidiabookstoreservice.book.dto.review.request.ReviewCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.response.ReviewListResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ReviewService {

    Page<ReviewListResponse> getReviewListByBookId(Long bookId, Long userId, Pageable pageable);

    void createReview(ReviewCreateRequest request, Long userId, List<MultipartFile> reviewImageList);

    List<Long> getReviewedOrderItemIdList(List<Long> orderItemIdList);

}
