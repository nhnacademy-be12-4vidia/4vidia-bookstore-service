package com.nhnacademy._vidiabookstoreservice.order.dto.order.response;


public record OrderBookResponse(
            Long bookId,
            String bookTitle,
            String bookAuthor,
            String bookImageUrl,
            String categoryKdc,
            Integer quantity,
            Integer salePrice
    ) {
        public static OrderBookResponse from(BookOrderResponse book, int quantity) {
            return new OrderBookResponse(
                    book.id(),
                    book.title(),
                    book.author(),
                    book.imageUrl(),
                    book.categoryKdc(),
                    quantity,
                    book.salePrice()
            );
        }
    }