package com.nhnacademy._vidiabookstoreservice.book.service;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.utils.BookSortKey;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookSearchRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookStockChangeRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.*;

import java.util.List;

import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.BookOrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {

    BookIdResponse createBook(BookCreateRequest request, MultipartFile thumbnail);

    Page<BookListResponse> getBookList(BookSearchRequest request, Pageable pageable);

    BookDetailResponse getBookDetail(Long id);

    Page<BookListResponse> getBookListByCategory(Long categoryId, Pageable pageable);

    Page<BookListResponse> getBookListByPublisher(Long publisherId, Pageable pageable);

    Page<BookListResponse> getBookListByAuthor(Long authorId, Pageable pageable);

    Page<BookListResponse> getBookListByCategoryPath(Long categoryId, Pageable pageable);

    Book getBookEntity(Long bookId);

    Book getProxyById(Long bookId);

    void decreaseStock(List<BookStockChangeRequest> bookStockChangeRequestList);

    void increaseStock(List<BookStockChangeRequest> bookStockChangeRequestList);

    void updateBook(Long bookId, BookUpdateRequest request, MultipartFile thumbnail);

    List<BookOrderResponse> getOrderBookByBookIds(List<Long> bookIds);

    List<BookListResponse> getBookListResponseByIdList(List<Long> bookIdList, Long userId);

    PageResponse<BaseBookListResponse> getBookListResponseByTagId(Long tagId, Long userId, Pageable pageable);

    PageResponse<BaseBookListResponse> getBooksByTag(Long tagId, String tagName, BookSortKey sortKey, boolean asc, Pageable pageable, Long userId);
}
