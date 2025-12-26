package com.nhnacademy._vidiabookstoreservice.book.repository.custom;

import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookListResponse;

import java.util.List;

public interface BookRepositoryCustom {

    List<BookListResponse> getMainPageCategoryBookList(Long categoryId, Long userId);

    List<BookListResponse> getMainPageNoCategoryBookList(Long userId);
}
