package com.nhnacademy._vidiabookstoreservice.book.service;

import com.nhnacademy._vidiabookstoreservice.book.dto.BookCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.BookIdDto;

public interface BookService {

    BookIdDto createBook(BookCreateRequest request);

}
