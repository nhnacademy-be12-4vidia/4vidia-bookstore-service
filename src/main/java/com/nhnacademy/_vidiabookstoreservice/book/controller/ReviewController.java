package com.nhnacademy._vidiabookstoreservice.book.controller;

import com.nhnacademy._vidiabookstoreservice.book.dto.review.request.ReviewCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.request.ReviewUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.response.ReviewListResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.review.response.ReviewListWithSummaryResponse;
import com.nhnacademy._vidiabookstoreservice.book.service.BookReviewSummaryService;
import com.nhnacademy._vidiabookstoreservice.book.service.ReviewService;
import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final BookReviewSummaryService bookReviewSummaryService;

    @PostMapping(
            value = "/books/{bookId}/reviews",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public void createReview(@PathVariable Long bookId,
                                      @ModelAttribute ReviewCreateRequest request, @RequestPart(value = "images", required = false) List<MultipartFile> reviewImageList) {
        Long userId = UserContext.get().getUserId();
        reviewService.createReview(request, userId, reviewImageList);

    }

    @GetMapping("/books/{book-id}/reviews")
    public ResponseEntity<ReviewListWithSummaryResponse> getReviewsWithSummary(@PathVariable(name = "book-id") Long bookId, Pageable pageable) {
        Long userId = UserContext.get().getUserId();
        Page<ReviewListResponse> reviewListResponsePage = reviewService.getReviewListByBookId(bookId, userId, pageable);

        PageResponse<ReviewListResponse> reviewList = PageResponse.from(reviewListResponsePage);
        String reviewSummary = bookReviewSummaryService.getSummary(bookId);

        return ResponseEntity.ok(new ReviewListWithSummaryResponse(reviewList, reviewSummary));
    }

    @PostMapping("/books/{book-id}/reviews/{review-id}/deactivate")
    public void deactivateReview(@PathVariable(name = "book-id") Long bookId, @PathVariable(name = "review-id") Long reviewId) {
        Long userId = UserContext.get().getUserId();

        reviewService.deactivateReview(userId, reviewId, bookId);

    }

    @PostMapping("/books/{book-id}/reviews/{review-id}/edit")
    public void editReview(@PathVariable(name = "book-id") Long bookId,
                                    @PathVariable(name = "review-id") Long reviewId,
                                    @ModelAttribute ReviewUpdateRequest request,
                                    @RequestPart(value = "images", required = false) List<MultipartFile> newImageList,
                                    @RequestHeader(value = "Referer", required = false) String referer) {
        Long userId = UserContext.get().getUserId();

        reviewService.updateReview(userId, reviewId, bookId, request, newImageList);
    }
}
