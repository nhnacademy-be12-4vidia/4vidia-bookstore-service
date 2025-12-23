package com.nhnacademy._vidiabookstoreservice.book.dto.search.response;

import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BaseBookListResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookSearchListResponse;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiBookSearchResponse {

    private PageResponse<BaseBookListResponse> results;
    private String aiAnswer;

}
