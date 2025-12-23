package com.nhnacademy._vidiabookstoreservice.book.dto.book.response;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
public class BookListResponse extends BaseBookListResponse {

    private boolean liked;

    public static BookListResponse from(Book book) {
        return fillBase(BookListResponse.builder(), book, null)
            .build();
    }

    public static BookListResponse from(Book book, boolean isLiked) {
        return fillBase(BookListResponse.builder(), book, "출판사 정보가 없습니다")
            .liked(isLiked)
            .build();
    }
}
