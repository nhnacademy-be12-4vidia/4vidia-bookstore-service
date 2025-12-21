package com.nhnacademy._vidiabookstoreservice.book.dto.review.response;

import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewListWithSummaryResponse {

    PageResponse<ReviewListResponse> reviews;
    String reviewSummary;
}
