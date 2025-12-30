package com.nhnacademy._vidiabookstoreservice.book.dto.search.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EsBookSearchRequest {

    @NotBlank(message = "검색어는 필수입니다.")
    private String keyword;
    private String sortKey;
    private String direction;
    private Long categoryId;
    private Integer minPrice;
    private Integer maxPrice;
    private Boolean useSemantic;

    @Builder
    public EsBookSearchRequest(String keyword) {
        this.keyword = keyword;
    }
}
