package com.nhnacademy._vidiabookstoreservice.order.dto.order.response;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;

public record OrderBookResponse(
            Long bookId,
            String bookTitle,
            String bookAuthor,
            String bookImageUrl,
            Integer quantity,
            Integer salePrice
    ) {
        public static OrderBookResponse from(Book book, int quantity) {
            return new OrderBookResponse(
                    book.getId(),
                    book.getTitle(),
                    book.getBookAuthorList().stream().findFirst().map(author -> author.getAuthor().getName()).orElse("저자 미상"),
                    book.getBookImageList().stream().findFirst().map(image -> image.getImageUrl()).orElse(null),
                    quantity,
                    book.getPriceSales()
            );
        }
    }