package com.nhnacademy._vidiabookstoreservice.book.dto.book.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookSearchRequest {

    private String keyword;
    private String categoryCode;
    private String sort;

}
