package com.nhnacademy._vidiabookstoreservice.book.service;

import com.nhnacademy._vidiabookstoreservice.book.dto.request.BookCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.request.BookSearchRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.response.BookDetailResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.response.BookIdResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.response.BookListResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {

    BookIdResponse createBook(BookCreateRequest request);

    Page<BookListResponse> getBookList(BookSearchRequest request, Pageable pageable);

    BookDetailResponse getBookDetail(Long id);

}
