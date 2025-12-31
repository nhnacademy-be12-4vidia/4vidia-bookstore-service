package com.nhnacademy._vidiabookstoreservice.book.service;

public interface BookReviewSummaryService {

    void runNightly(int threshold, int limit);

    String getSummary(Long bookId);
}
