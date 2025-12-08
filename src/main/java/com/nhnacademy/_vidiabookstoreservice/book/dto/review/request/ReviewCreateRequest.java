package com.nhnacademy._vidiabookstoreservice.book.dto.review.request;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.Review;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

@Getter
@NoArgsConstructor
@Setter
public class ReviewCreateRequest {

    private String content;

    @NotNull
    private Long bookId;

    @NotNull
    private Long orderItemId;

    @Range(min = 1, max = 5)
    private Integer rating;

    public Review toEntity(OrderItem orderItem, Book book, User user,
        boolean hasPhoto) {

        return Review.builder()
            .user(user)
            .book(book)
            .content(this.content)
            .orderItem(orderItem)
            .hasPhoto(false)
            .rating(this.rating)
            .build();
    }

}
