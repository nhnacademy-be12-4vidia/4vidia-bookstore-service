package com.nhnacademy._vidiabookstoreservice.book.service.scheduler;

import com.nhnacademy._vidiabookstoreservice.book.service.BookReviewSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewSummaryScheduler {
    private final BookReviewSummaryService reviewSummaryService;

    @Scheduled(cron = "0 0 * * * *")
    public void runNightlyReviewSummary() {
        int threshold = 5;
        int limit = 200;

        log.info("[ReviewSummaryScheduler] nightly job start. threshold = {}, limit = {}", threshold, limit);
        reviewSummaryService.runNightly(threshold, limit);
        log.info("[ReviewSummaryScheduler] nightly job end.");
    }

}
