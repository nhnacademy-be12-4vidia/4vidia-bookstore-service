package com.nhnacademy._vidiabookstoreservice.book.controller;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookBestRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookDetailResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookDetailWithReviewResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookListResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookSearchListResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.response.ReviewListResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.search.request.EsBookSearchRequest;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import com.nhnacademy._vidiabookstoreservice.book.service.ReviewService;
import com.nhnacademy._vidiabookstoreservice.book.service.search.BookSearchService;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final BookSearchService bookSearchService;
    private final ReviewService reviewService;
    private final StringRedisTemplate bestsellerRedisTemplate;

//    public BookController(BookService bookService,
//                          BookSearchService bookSearchService,
//                          ReviewService reviewService,
//                          @Qualifier("bestsellerRedisTemplate") StringRedisTemplate redisTemplate) {
//        this.bookService = bookService;
//        this.bookSearchService = bookSearchService;
//        this.reviewService = reviewService;
//        this.redisTemplate = redisTemplate;
//    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<BookSearchListResponse>> searchBooks(
        @Valid EsBookSearchRequest request,
        @PageableDefault(size = 20) Pageable pageable,
        @RequestHeader(name = "X-User-Id", required = false) Long userId
    ) {
        Page<BookSearchListResponse> result = bookSearchService.searchBooks(request, pageable,
            userId);

        return ResponseEntity.ok(PageResponse.from(result));
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookDetailWithReviewResponse> bookDetails(@PathVariable Long bookId,
        @RequestHeader(name = "X-User-Id", required = false) Long userId, Pageable pageable) {

        Page<ReviewListResponse> reviewListResponsePage = reviewService.getReviewListByBookId(
            bookId, userId, pageable);

        PageResponse<ReviewListResponse> reviewList = PageResponse.from(reviewListResponsePage);
        BookDetailResponse bookDetailResponse = bookService.getBookDetail(bookId);

        return ResponseEntity.ok(BookDetailWithReviewResponse.of(bookDetailResponse, reviewList));
    }

    @GetMapping("/best-seller")
    public ResponseEntity<List<BookListResponse>> getBestSellers() {
        ZSetOperations<String, String> zSetOps = bestsellerRedisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> savedTop10 = zSetOps.reverseRangeWithScores("top10", 0, -1);
        List<Long> bookIdList = savedTop10.stream()
                .map(top10 -> Long.parseLong(top10.getValue()))
                .toList();

        List<BookListResponse> bestSellerList = bookService.getBookListResponseByIdList(bookIdList);

        return ResponseEntity.ok().body(bestSellerList);
    }
}
