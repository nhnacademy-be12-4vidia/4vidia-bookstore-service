package com.nhnacademy._vidiabookstoreservice.book.dto.book.response;

import com.nhnacademy._vidiabookstoreservice.book.dto.review.response.ReviewListResponse;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookDetailWithReviewResponse {

    BookDetailResponse book;
    PageResponse<ReviewListResponse> reviews;

    public static BookDetailWithReviewResponse of(BookDetailResponse book,
        PageResponse<ReviewListResponse> reviews) {
        return new BookDetailWithReviewResponse(book, reviews);
    }

}
