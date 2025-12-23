package com.nhnacademy._vidiabookstoreservice.book.dto.book.response;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
public class BookSearchListResponse extends BaseBookListResponse{


    private Boolean liked;
    private Integer rank;
    private Double relevanceScore;
    private boolean recommended;
    private String llmSummary;


    public static BookSearchListResponse from(Book book, boolean liked) {
        return fillBase(BookSearchListResponse.builder(), book, "출판사 정보 없음")
                .liked(liked)
                .build();
    }
}
