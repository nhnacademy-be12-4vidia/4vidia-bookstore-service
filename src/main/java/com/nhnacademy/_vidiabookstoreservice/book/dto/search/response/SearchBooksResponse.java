package com.nhnacademy._vidiabookstoreservice.book.dto.search.response;

import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BaseBookListResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookSearchListResponse;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;

import java.util.List;

public record SearchBooksResponse(
        PageResponse<BaseBookListResponse> page,
        List<AiCacheResponse> aiCacheResponseList
) {}
