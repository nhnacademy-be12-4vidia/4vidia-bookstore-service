package com.nhnacademy._vidiabookstoreservice.book.dto.response;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookListResponse {
    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "ISBN은 필수입니다")
    private String isbn;

    @NotNull(message = "판매가는 필수입니다.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer priceSales;

    @NotBlank(message = "작가명은 필수입니다.")
    private String authorName;

    @NotBlank(message = "출판사 이름은 필수입니다.")
    private String publisherName;

    @NotBlank(message = "이미지 url은 필수입니다.")
    private String imageUrl;


}
