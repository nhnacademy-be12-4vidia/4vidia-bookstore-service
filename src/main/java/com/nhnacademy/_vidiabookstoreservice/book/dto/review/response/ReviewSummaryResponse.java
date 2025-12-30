package com.nhnacademy._vidiabookstoreservice.book.dto.review.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewSummaryResponse {
    private Long bookId;
    private String summarizedReview;

    public static ReviewSummaryResponse of(Long bookId, String summarizedReview) {
        return ReviewSummaryResponse.builder()
                .bookId(bookId)
                .summarizedReview(summarizedReview)
                .build();
    }
}
