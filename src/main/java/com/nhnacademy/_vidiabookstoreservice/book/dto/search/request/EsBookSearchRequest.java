package com.nhnacademy._vidiabookstoreservice.book.dto.search.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EsBookSearchRequest {

    @NotBlank(message = "검색어는 필수입니다.")
    private String keyword;
    private String sort;
    private Long categoryId;
    private Integer minPrice;
    private Integer maxPrice;

}
