package com.nhnacademy._vidiabookstoreservice.book.dto.category.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateCategoryRequest(
    @NotBlank
    String categoryName
) {}