package com.nhnacademy._vidiabookstoreservice.book.dto.category.response;

import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryListResponse {

    private Long id;
    private String kdcCode;
    private String categoryName;
    private Integer depth;

    public static CategoryListResponse from(Category category) {
        return CategoryListResponse.builder()
            .id(category.getId())
            .kdcCode(category.getKdcCode())
            .categoryName(category.getCategoryName())
            .depth(category.getDepth())
            .build();
    }
}
