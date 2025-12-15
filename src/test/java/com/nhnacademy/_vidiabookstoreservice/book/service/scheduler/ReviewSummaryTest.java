package com.nhnacademy._vidiabookstoreservice.book.service.scheduler;

import com.nhnacademy._vidiabookstoreservice.book.ai.gemini.GeminiAnswerService;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookReviewSummary;
import com.nhnacademy._vidiabookstoreservice.book.domain.Review;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookReviewSummaryRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.ReviewRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.ReviewService;
import com.nhnacademy._vidiabookstoreservice.book.service.impl.BookReviewSummaryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ReviewSummaryTest {
    @Mock
    BookReviewSummaryRepository summaryRepository;
    @Mock
    ReviewRepository reviewRepository;
    @Mock
    GeminiAnswerService geminiAnswerService;
    @Mock
    ReviewService reviewService;

    @InjectMocks
    BookReviewSummaryServiceImpl service;

    @Test
    void runNightly_summarize_when_dirtyCount_reaches_threshold() {
        Long bookId = 1L;

        BookReviewSummary s = BookReviewSummary.builder().bookId(bookId).build();

        for (int i = 0; i < 5; i++) s.markDirty();

        when(summaryRepository.findTargets(eq(5), any())).thenReturn(List.of(s));

        when(summaryRepository.tryMarkRunning(bookId)).thenReturn(1);

        Review r1 = mock(Review.class);
        when(r1.getId()).thenReturn(100L);
        when(r1.getContent()).thenReturn("좋아요");

        Review r2 = mock(Review.class);
        when(r2.getContent()).thenReturn("별로예요");

        Review r3 = mock(Review.class);
        when(r3.getContent()).thenReturn("가성비 좋습니다");

        Review r4 = mock(Review.class);
        when(r4.getContent()).thenReturn("배송 빨라요");

        Review r5 = mock(Review.class);
        when(r5.getContent()).thenReturn("내용이 탄탄해요");

        when(reviewService.getTop200ReviewListByBookId(bookId))
                .thenReturn(List.of(r1, r2, r3, r4, r5));

        when(geminiAnswerService.summarizeReview(eq(bookId), anyString()))
                .thenReturn("총평: 테스트 요약\n장점: a;b\n단점: c\n추천대상: d");

        service.runNightly(5, 200);

        verify(geminiAnswerService, times(1)).summarizeReview(eq(bookId), anyString());

    }

}