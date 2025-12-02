package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import com.nhnacademy._vidiabookstoreservice.book.dto.category.response.CategoryListResponse;
import com.nhnacademy._vidiabookstoreservice.book.exception.CategoryNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.CategoryRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryListResponse> getCategoryList() {

        List<Category> categoryList = categoryRepository.findAll();
        return categoryList.stream().map(CategoryListResponse::from).toList();
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
