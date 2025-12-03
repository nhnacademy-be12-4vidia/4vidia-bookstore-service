package com.nhnacademy._vidiabookstoreservice.cart.dto.response;

public record CartBookResponse(
        BookSummaryResponse book,
        Integer quantity
) {
    public static CartBookResponse of(BookSummaryResponse book, int quantity) {
        return new CartBookResponse(book, quantity);
    }
}

