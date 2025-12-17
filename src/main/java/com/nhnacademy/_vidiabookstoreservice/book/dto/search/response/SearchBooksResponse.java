package com.nhnacademy._vidiabookstoreservice.book.dto.search.response;

import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookSearchListResponse;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;

import java.util.List;

public record SearchBooksResponse(
        PageResponse<BookSearchListResponse> page,
        List<AiCacheResponse> aiCacheResponseList
) {}
