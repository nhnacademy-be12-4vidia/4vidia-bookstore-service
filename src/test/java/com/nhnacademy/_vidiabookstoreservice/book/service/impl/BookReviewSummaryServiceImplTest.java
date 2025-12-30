package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.nhnacademy._vidiabookstoreservice.book.ai.gemini.GeminiAnswerService;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookReviewSummary;
import com.nhnacademy._vidiabookstoreservice.book.domain.Review;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookReviewSummaryRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.ReviewService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BookReviewSummaryServiceImplTest {

    @Mock
    private BookReviewSummaryRepository bookReviewSummaryRepository;

    @Mock
    private ReviewService reviewService;

    @Mock
    private GeminiAnswerService geminiAnswerService;

    @InjectMocks
    private BookReviewSummaryServiceImpl bookReviewSummaryService;

    @Test
    @DisplayName("리뷰 요약 실행 - 성공")
    void runNightly_success() {
        Long bookId = 1L;
        int threshold = 5;
        int limit = 10;

        BookReviewSummary summary = spy(new BookReviewSummary(bookId));
        ReflectionTestUtils.setField(summary, "dirtyCount", 5);

        Review review1 = mock(Review.class);
        Review review2 = mock(Review.class);
        when(review1.getId()).thenReturn(100L);
        when(review1.getContent()).thenReturn("좋은 책입니다.");
        when(review2.getContent()).thenReturn("추천해요.");

        when(bookReviewSummaryRepository.findTargets(eq(threshold), any(PageRequest.of(0, limit).getClass())))
                .thenReturn(List.of(summary));
        when(bookReviewSummaryRepository.tryMarkRunning(bookId)).thenReturn(1);
        when(reviewService.getTop200ReviewListByBookId(bookId)).thenReturn(List.of(review1, review2));
        when(geminiAnswerService.summarizeReview(eq(bookId), anyString())).thenReturn("요약된 내용");

        bookReviewSummaryService.runNightly(threshold, limit);

        verify(geminiAnswerService).summarizeReview(eq(bookId), contains("좋은 책입니다."));
        verify(summary).markSuccess(eq("요약된 내용"), eq(100L));
    }

    @Test
    @DisplayName("리뷰 요약 실행 - AI 서비스 실패 시 markFail 호출")
    void runNightly_aiFailure_marksFail() {
        Long bookId = 1L;
        BookReviewSummary summary = new BookReviewSummary(bookId);
        ReflectionTestUtils.setField(summary, "dirtyCount", 5);

        when(bookReviewSummaryRepository.findTargets(anyInt(), any(PageRequest.class)))
                .thenReturn(List.of(summary));
        when(bookReviewSummaryRepository.tryMarkRunning(bookId)).thenReturn(1);

        Review review = mock(Review.class);
        when(review.getContent()).thenReturn("리뷰내용");
        when(reviewService.getTop200ReviewListByBookId(bookId)).thenReturn(List.of(review));

        when(geminiAnswerService.summarizeReview(anyLong(), anyString()))
                .thenThrow(new RuntimeException("AI Error"));

        BookReviewSummary summaryInDb = spy(new BookReviewSummary(bookId));
        when(bookReviewSummaryRepository.findById(bookId)).thenReturn(Optional.of(summaryInDb));

        bookReviewSummaryService.runNightly(5, 10);

        verify(summaryInDb).markFail();
    }

    @Test
    @DisplayName("요약 텍스트 조회 - 성공")
    void getSummary_returnsText() {
        when(bookReviewSummaryRepository.findNonEmptySummaryText(1L))
                .thenReturn(Optional.of("요약본"));

        String result = bookReviewSummaryService.getSummary(1L);

        assertEquals("요약본", result);
    }
}