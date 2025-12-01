package com.nhnacademy._vidiabookstoreservice.book.dto.book.event;

public record BookStockChangedEvent(
    Long bookId,
    Integer newStock
) {}
