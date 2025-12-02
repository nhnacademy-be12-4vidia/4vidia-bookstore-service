package com.nhnacademy._vidiabookstoreservice.user.dto.like.response;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.user.domain.Like;

public record LikeResponse(
        Long bookId,
        String bookTitle, // 책 제목
        String authorName, // 저자
        Integer priceStandard, // 판매가
        Integer priceSales, // 할인가
        String stockStatus, // 재고상태(품절인지)
        String bookImage // 책 이미지
){
    public static LikeResponse fromEntity(Like like) {
        Book book = like.getBook();

        String authorName = book.getBookAuthorList().stream()
                .findFirst()
                .map(author -> author.getAuthor().getName())
                .orElse("저자 미상");

        String bookImage = book.getBookImageList().stream()
                .findFirst()
                .map(image -> image.getImageUrl())
                .orElse("/img/default-book.png"); // 이미지 없을 때 넣을거

        return new LikeResponse(
                book.getId(),
                book.getTitle(),
                authorName,
                book.getPriceStandard(),
                book.getPriceSales(),
                book.getStockStatus().name(),
                bookImage
        );
    }
}