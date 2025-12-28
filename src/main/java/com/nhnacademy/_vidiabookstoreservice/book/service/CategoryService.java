package com.nhnacademy._vidiabookstoreservice.book.service;

import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import com.nhnacademy._vidiabookstoreservice.book.dto.category.response.CategoryListResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CategoryService {

    List<CategoryListResponse> getCategoryList();

    @Transactional(readOnly = true)
    List<CategoryListResponse> getFlatCategoryList();

    Category getCategory(Long categoryId);

    Category getCategoryProxy(Long categoryId);

}
