package com.nhnacademy._vidiabookstoreservice.book.dto.category.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateCategoryRequest(
    @NotBlank
    @Pattern(regexp = "^[A-Z0-9]{3}$", message = "KDC 코드는 3자리의 숫자 또는 대문자여야 합니다.")
    String kdcCode,

    @NotBlank
    String categoryName
) {}
