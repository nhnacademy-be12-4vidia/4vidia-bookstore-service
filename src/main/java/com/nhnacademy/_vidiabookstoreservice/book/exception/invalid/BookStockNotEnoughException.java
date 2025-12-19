package com.nhnacademy._vidiabookstoreservice.book.exception.invalid;

import com.nhnacademy._vidiabookstoreservice.book.exception.BookErrorCode;
import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class BookStockNotEnoughException extends BaseException {

    public BookStockNotEnoughException() {
        super(BookErrorCode.BOOK_STOCK_NOT_ENOUGH);
    }
}
