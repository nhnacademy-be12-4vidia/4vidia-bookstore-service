package com.nhnacademy._vidiabookstoreservice.book.repository.custom;

import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BaseBookListResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookListResponse;

import java.util.List;

public interface BookRepositoryCustom {

    List<BookListResponse> getMainPageResult(Long categoryId, Long userId);
}
