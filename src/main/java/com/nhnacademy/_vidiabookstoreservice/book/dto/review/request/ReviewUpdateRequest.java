package com.nhnacademy._vidiabookstoreservice.book.dto.review.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.Range;

@Getter
@Builder(toBuilder = true)
public class ReviewUpdateRequest {

    @NotNull
    private Long reviewId;

    @NotNull
    private Long bookId;

    private String content;

    @Range(min = 1, max = 5)
    private Integer rating;
}
