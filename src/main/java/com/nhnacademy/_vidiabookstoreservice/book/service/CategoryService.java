package com.nhnacademy._vidiabookstoreservice.book.service;

import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import com.nhnacademy._vidiabookstoreservice.book.dto.category.request.CreateCategoryRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.category.request.UpdateCategoryRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.category.response.CategoryListResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CategoryService {

    List<CategoryListResponse> getCategoryList();

    @Transactional(readOnly = true)
    List<CategoryListResponse> getFlatCategoryList();

    Category getCategory(Long categoryId);

    Category getCategoryProxy(Long categoryId);

    void createCategory(CreateCategoryRequest request);

    void updateCategory(Long categoryId, UpdateCategoryRequest request);

    void deleteCategory(Long categoryId);
}
