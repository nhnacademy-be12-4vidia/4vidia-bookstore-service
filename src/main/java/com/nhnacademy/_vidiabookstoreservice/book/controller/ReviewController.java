package com.nhnacademy._vidiabookstoreservice.book.controller;

import com.nhnacademy._vidiabookstoreservice.book.dto.review.request.ReviewCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.service.ReviewService;
import java.util.List;

import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping(
        value = "/books/{bookId}/reviews",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )

    ResponseEntity<Void> createReview(@PathVariable Long bookId,
        @ModelAttribute ReviewCreateRequest request, @RequestPart(value = "images", required = false) List<MultipartFile> reviewImageList) {
        Long userId = UserContext.get().getUserId();
        reviewService.createReview(request, userId, reviewImageList);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
