package com.nhnacademy._vidiabookstoreservice.book.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotEnoughException;

public class BookStockNotEnoughException extends NotEnoughException {

    public BookStockNotEnoughException(String message) {
        super(message);
    }
}
