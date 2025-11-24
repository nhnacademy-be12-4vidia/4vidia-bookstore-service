package com.nhnacademy._vidiabookstoreservice.book.service;

import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookSearchRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookDetailResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookIdResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {

    BookIdResponse createBook(BookCreateRequest request);

    Page<BookListResponse> getBookList(BookSearchRequest request, Pageable pageable);

    BookDetailResponse getBookDetail(Long id);

}
