package com.nhnacademy._vidiabookstoreservice.book.dto.review.response;

import com.nhnacademy._vidiabookstoreservice.book.domain.Review;
import com.nhnacademy._vidiabookstoreservice.book.domain.ReviewImage;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewListResponse {

    private Long reviewId;

    private Long userId;
    private String userName;

    private String content;
    private Integer rating;

    private List<String> imageUrlList;
    private LocalDate createdAt;

    private boolean isMyReview;

    public static ReviewListResponse of(Review review, Long currentUserId) {
        return ReviewListResponse.builder()
            .reviewId(review.getId())
            .userId(review.getUser().getUserId())
            .userName(review.getUser().getName())
            .content(review.getContent())
            .rating(review.getRating())
            .imageUrlList(review.getImageList().stream().map(ReviewImage::getImageUrl).toList())
            .createdAt(review.getCreatedAt())
            .isMyReview(review.getUser().getUserId().equals(currentUserId))
            .build();
    }
}
