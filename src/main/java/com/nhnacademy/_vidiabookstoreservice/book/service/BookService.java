package com.nhnacademy._vidiabookstoreservice.book.service;

import com.nhnacademy._vidiabookstoreservice.book.dto.request.BookCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.response.BookIdResponse;

public interface BookService {

    BookIdResponse createBook(BookCreateRequest request);

}
