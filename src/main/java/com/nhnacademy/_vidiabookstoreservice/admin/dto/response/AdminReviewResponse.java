package com.nhnacademy._vidiabookstoreservice.admin.dto.response;

import com.nhnacademy._vidiabookstoreservice.book.domain.Review;
import com.nhnacademy._vidiabookstoreservice.book.domain.ReviewImage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AdminReviewResponse(
        Long id,
        Long bookId,
        String bookTitle,
        String email,
        String userNickname,
        Integer rating,
        String content,
        boolean hasPhoto, // 사진 여부
        List<String> imageUrls,
        LocalDate createdAt

) {
    public static AdminReviewResponse from(Review review) {
        var user = review.getUser();
        var book = review.getBook();

        List<String> urls = review.getImageList().stream()
                .map(ReviewImage::getImageUrl)
                .toList();

        boolean hasPhoto = !urls.isEmpty();

        return new AdminReviewResponse(
                review.getId(),
                book.getId(),
                book.getTitle(),
                user.getEmail(),
                user.getName(),
                review.getRating(),
                review.getContent(),
                hasPhoto,
                urls,
                review.getCreatedAt()
        );
    }
}