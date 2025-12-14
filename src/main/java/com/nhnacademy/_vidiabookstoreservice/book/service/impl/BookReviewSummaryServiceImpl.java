package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.BookReviewSummary;
import com.nhnacademy._vidiabookstoreservice.book.domain.Review;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookReviewSummaryRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.BookReviewSummaryService;
import com.nhnacademy._vidiabookstoreservice.book.service.ReviewService;
import com.nhnacademy._vidiabookstoreservice.book.service.search.GeminiAnswerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookReviewSummaryServiceImpl implements BookReviewSummaryService {

    private final BookReviewSummaryRepository bookReviewSummaryRepository;
    private final ReviewService reviewService;
    private final GeminiAnswerService geminiAnswerService;
    private final int MAX_CHARS = 12_000;

    @Override
    @Transactional
    public void runNightly(int threshold, int limit) {
        List<BookReviewSummary> targets = bookReviewSummaryRepository.findTargets(threshold, PageRequest.of(0, limit));

        for (BookReviewSummary s : targets) {
            int updated = bookReviewSummaryRepository.tryMarkRunning(s.getBookId());
            if (updated == 0) continue;

            try {
                if (s.getDirtyCount() < threshold) continue;

                List<Review> reviewList = reviewService.getTop200ReviewListByBookId(s.getBookId());

                String reviewTextBundle = reviewList.stream()
                        .map(Review::getContent)
                        .filter(c -> c != null && !c.isBlank())
                        .map(String::trim)
                        .collect(java.util.stream.Collectors.joining("\n"));

                if (reviewTextBundle.isBlank()) {
                    continue;
                }

                if (reviewTextBundle.length() > MAX_CHARS) {
                    reviewTextBundle = reviewTextBundle.substring(0, MAX_CHARS);
                }

                String summaryText = geminiAnswerService.summarizeReview(s.getBookId(), reviewTextBundle);
                Long lastReviewId = reviewList.isEmpty() ? null : reviewList.getFirst().getId();

                s.markSuccess(summaryText, lastReviewId);

            } catch (Exception e) {
                bookReviewSummaryRepository.findById(s.getBookId())
                        .ifPresent(BookReviewSummary::markFail);
                log.error("Review summary failed. bookId={}", s.getBookId(), e);
            }

        }
    }
}
