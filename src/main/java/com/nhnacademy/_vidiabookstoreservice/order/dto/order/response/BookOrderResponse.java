package com.nhnacademy._vidiabookstoreservice.order.dto.order.response;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;

import java.util.Optional;

public record BookOrderResponse(
        Long id,
        String title,
        String author,
        String imageUrl,
        Integer salePrice,
        Long category
) {
    public static BookOrderResponse from(Book book) {
        return new BookOrderResponse(
            book.getId(),
            book.getTitle(),
            book.getBookAuthorList().stream().findFirst().map(author -> author.getAuthor().getName()).orElse("저자 미상"),
            book.getBookImageList().stream().findFirst().map(bookImage -> bookImage.getImageUrl().toString()).orElse("null"),
            book.getPriceSales(),
            Optional.ofNullable(book.getCategory())
                        .map(category -> category.getId())
                        .orElse(null)
        );
    }
}
