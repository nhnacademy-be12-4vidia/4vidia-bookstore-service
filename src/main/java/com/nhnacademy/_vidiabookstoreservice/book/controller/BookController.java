package com.nhnacademy._vidiabookstoreservice.book.controller;

import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookDetailResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookDetailWithReviewResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookListResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookSearchListResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.response.ReviewListResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.response.ReviewSummaryResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.search.request.EsBookSearchRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.search.response.AiBookSearchResponse;
import com.nhnacademy._vidiabookstoreservice.book.service.BookReviewSummaryService;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import com.nhnacademy._vidiabookstoreservice.book.service.ReviewService;
import com.nhnacademy._vidiabookstoreservice.book.service.search.BookSearchService;
import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
import jakarta.validation.Valid;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService bookService;
    private final BookSearchService bookSearchService;
    private final ReviewService reviewService;
    private final StringRedisTemplate bestsellerRedisTemplate;
    private final BookReviewSummaryService bookReviewSummaryService;


    @GetMapping("/search")
    public ResponseEntity<PageResponse<BookSearchListResponse>> searchBooks(
        @Valid @ModelAttribute EsBookSearchRequest request,
        @PageableDefault(size = 20) Pageable pageable,
        @RequestHeader(name = "X-User-Id", required = false) Long userId
    ) {
        Page<BookSearchListResponse> result = bookSearchService.searchBooks(request, pageable,
            userId);

        return ResponseEntity.ok(PageResponse.from(result));
    }

    @GetMapping("/search/ai")
    public ResponseEntity<AiBookSearchResponse> searchBooksWithLlm(
        @Valid EsBookSearchRequest request,
        @PageableDefault(size = 20) Pageable pageable,
        @RequestHeader(name = "X-User-Id", required = false) Long userId) {

        AiBookSearchResponse response = bookSearchService.searchBookWithLlm(request, pageable,
            userId);

        return ResponseEntity.ok(response);

    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookDetailWithReviewResponse> bookDetails(@PathVariable Long bookId, Pageable pageable) {
        Long userId = UserContext.get().getUserId();
        Page<ReviewListResponse> reviewListResponsePage = reviewService.getReviewListByBookId(
            bookId, userId, pageable);

        PageResponse<ReviewListResponse> reviewList = PageResponse.from(reviewListResponsePage);
        BookDetailResponse bookDetailResponse = bookService.getBookDetail(bookId);
        String reviewSummary = bookReviewSummaryService.getSummary(bookId);

        return ResponseEntity.ok(BookDetailWithReviewResponse.of(bookDetailResponse, reviewList, reviewSummary));
    }

    @GetMapping("/best-seller")
    public ResponseEntity<List<BookListResponse>> getBestSellers() {
        Long userId = null;
        if (UserContext.get() != null) {
            userId = UserContext.get().getUserId();
        }

        ListOperations<String, String> listOps = bestsellerRedisTemplate.opsForList();
//        List<String> savedTop10 = listOps.range("top10", 0, -1);
        List<String> savedTop10 = listOps.range("view:bestseller:top10", 0, -1);
        if (savedTop10 == null || savedTop10.isEmpty()) {
            return ResponseEntity.ok().body(Collections.emptyList());
        }

        List<Long> bookIdList = savedTop10.stream()
                .map(Long::parseLong)
                .toList();

        List<BookListResponse> bestSellerList = bookService.getBookListResponseByIdList(bookIdList, userId);

        return ResponseEntity.ok().body(bestSellerList);
    }
}
