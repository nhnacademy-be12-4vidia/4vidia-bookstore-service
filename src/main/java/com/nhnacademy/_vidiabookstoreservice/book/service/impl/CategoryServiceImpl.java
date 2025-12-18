package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import com.nhnacademy._vidiabookstoreservice.book.dto.category.response.CategoryListResponse;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.CategoryNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.CategoryRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Cacheable(cacheNames = "categoryList")
    @Transactional(readOnly = true)
    public List<CategoryListResponse> getCategoryList() {

        List<Category> categoryList = categoryRepository.findAll();
        return categoryList.stream().map(CategoryListResponse::from).toList();
    }

    @Override
    @Cacheable(cacheNames = "flatCategoryList")
    @Transactional(readOnly = true)
    public List<CategoryListResponse> getFlatCategoryList() {

        List<Category> categoryList = categoryRepository.findAllByOrderByKdcCodeAsc();
        return categoryList.stream()
                .filter(category -> category.getCategoryName() != null)
                .map(this::toCategoryListResponseWithFullName)
                .toList();
    }

    private CategoryListResponse toCategoryListResponseWithFullName(Category category) {
        StringBuilder fullName = new StringBuilder(category.getCategoryName());
        Category parent = category.getParentCategory();

        while (parent != null) {
            if (parent.getCategoryName() != null) {
                fullName.insert(0, parent.getCategoryName() + " > ");
            }
            parent = parent.getParentCategory();
        }

        return CategoryListResponse.builder()
            .id(category.getId())
            .kdcCode(category.getKdcCode())
            .categoryName(fullName.toString())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(
            () -> new CategoryNotFoundException(categoryId));
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryProxy(Long categoryId) {
        return categoryRepository.getReferenceById(categoryId);
    }
}
