package com.nhnacademy._vidiabookstoreservice.book.dto.book.event;

public record BookSavedEvent(
    Long bookId,
    String title
) {}
