package com.nhnacademy._vidiabookstoreservice.book.service;

import com.nhnacademy._vidiabookstoreservice.book.dto.review.response.ReviewSummaryResponse;

public interface BookReviewSummaryService {

    void runNightly(int threshold, int limit);

    String getSummary(Long bookId);
}
