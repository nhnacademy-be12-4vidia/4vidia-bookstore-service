package com.nhnacademy._vidiabookstoreservice.book.controller;

import com.nhnacademy._vidiabookstoreservice.book.dto.book.BookSortKey;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.*;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.response.ReviewListResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.response.ReviewSummaryResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.search.request.EsBookSearchRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.search.request.EsBookSearchWithTagRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.search.response.AiBookSearchResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.search.response.SearchBooksResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<SearchBooksResponse> searchBooks(
        @Valid @ModelAttribute EsBookSearchRequest request,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        Long userId = UserContext.get().getUserId();
        SearchBooksResponse result = bookSearchService.searchBooks(request, pageable,
            userId);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/ai")
    public ResponseEntity<AiBookSearchResponse> searchBooksWithLlm(
        @Valid EsBookSearchRequest request,
        @PageableDefault(size = 20) Pageable pageable) {
        Long userId = UserContext.get().getUserId();
        AiBookSearchResponse response = bookSearchService.searchBookWithLlm(request, pageable,
            userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/tags")
    public ResponseEntity<PageResponse<BaseBookListResponse>> searchBooksWithTags(
            @Valid EsBookSearchWithTagRequest request,
            @PageableDefault(size = 20) Pageable pageable) {

        Long userId = UserContext.get().getUserId();
        PageResponse<BaseBookListResponse> result = bookSearchService.searchBooksByTags(request, pageable, userId);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/tags/{tag-id}")
    public ResponseEntity<PageResponse<BaseBookListResponse>> searchBooksWithSpecificTag(
            @PathVariable(name = "tag-id") Long tagId,
            @RequestParam(required = false) String tagName,
            @RequestParam(required = false) String sortKey,
            @RequestParam(required = false) String direction,
            @PageableDefault(size = 20) Pageable pageable
            ) {
        Long userId = UserContext.get().getUserId();

        BookSortKey sort = BookSortKey.from(sortKey);
        boolean asc = "asc".equalsIgnoreCase(direction);

        return ResponseEntity.ok(bookService.getBooksByTag(tagId, tagName, sort, asc, pageable, userId));

    }

    @GetMapping("/{bookId:\\d+}")
    public ResponseEntity<BookDetailResponse> bookDetails(@PathVariable Long bookId) {
        BookDetailResponse bookDetailResponse = bookService.getBookDetail(bookId);

        return ResponseEntity.ok(bookDetailResponse);
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
