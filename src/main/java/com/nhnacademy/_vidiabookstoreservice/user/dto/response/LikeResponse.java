package com.nhnacademy._vidiabookstoreservice.user.dto.response;

import com.nhnacademy._vidiabookstoreservice.book.domain.BookAuthor;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookImage;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.StockStatus;
import com.nhnacademy._vidiabookstoreservice.user.domain.Like;

import java.util.List;

public record LikeResponse(
        Long bookId,
        String bookTitle, // 책 제목
        List<BookAuthor> bookAuthors, // 저자
        Integer priceStandard, // 판매가
        Integer priceSales, // 할인가
        StockStatus stockStatus, // 재고상태(품절인지)
        List<BookImage> bookImageList // 책 이미지
){
    public static LikeResponse fromEntity(Like like) {
        return new LikeResponse(
                like.getBook().getId(),
                like.getBook().getTitle(),
                like.getBook().getBookAuthors(),
                like.getBook().getPriceStandard(),
                like.getBook().getPriceSales(),
                like.getBook().getStockStatus(),
                like.getBook().getBookImageList()
        );
    }
}