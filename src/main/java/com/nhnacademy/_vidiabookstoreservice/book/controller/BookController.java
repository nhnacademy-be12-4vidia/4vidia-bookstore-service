package com.nhnacademy._vidiabookstoreservice.book.controller;

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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("/search")
    public ResponseEntity<PageResponse<BookSearchListResponse>> searchBooks(
        @Valid EsBookSearchRequest request,
        @PageableDefault(size = 20) Pageable pageable,
        @RequestHeader(name = "X-User-Id", required = false) Long userId
    ) {
        Page<BookSearchListResponse> result = bookSearchService.searchBooks(request, pageable, userId);

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
}
