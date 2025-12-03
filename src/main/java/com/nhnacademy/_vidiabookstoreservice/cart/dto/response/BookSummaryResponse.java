package com.nhnacademy._vidiabookstoreservice.cart.dto.response;


import com.nhnacademy._vidiabookstoreservice.book.domain.Book;

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
               null
                // TODO 표지 가져오기
        );
    }
}


