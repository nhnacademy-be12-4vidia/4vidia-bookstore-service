package com.nhnacademy._vidiabookstoreservice.cart.dto.response;


import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookImage;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.ImageType;

public record BookSummaryResponse(
        Long id,
        String title,
        Integer priceStandard,
        Integer priceSales,
        String imageUrl // 표지 이미지만 필요
) {
    public static BookSummaryResponse from(Book book) {
        return new BookSummaryResponse(
                book.getId(),
                book.getTitle(),
                book.getPriceStandard(),
                book.getPriceSales(),
                (book.getBookImageList().stream().filter(i -> i.getImageType().equals(
                        ImageType.THUMBNAIL)).findFirst().map(BookImage::getImageUrl).orElse(null))
        );
    }
}


