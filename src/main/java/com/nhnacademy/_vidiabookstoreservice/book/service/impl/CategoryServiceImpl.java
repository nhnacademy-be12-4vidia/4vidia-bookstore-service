package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import com.nhnacademy._vidiabookstoreservice.book.dto.category.request.CreateCategoryRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.category.request.UpdateCategoryRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.category.response.CategoryListResponse;
import com.nhnacademy._vidiabookstoreservice.book.exception.already.CategoryAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.exception.delete.CategoryCannotDeleteException;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.CategoryNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.ParentCategoryNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.CategoryRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.DiscountPolicyRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.CategoryService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final DiscountPolicyRepository discountPolicyRepository;

    @Override
    @Cacheable(cacheNames = "categoryList", cacheManager = "categoryListCacheManager")
    @Transactional(readOnly = true)
    public List<CategoryListResponse> getCategoryList() {

        List<Category> categoryList = categoryRepository.findAllByOrderByKdcCodeAsc();
        return categoryList.stream()
                .map(CategoryListResponse::from)
                .toList();
    }

    @Override
    @Cacheable(cacheNames = "flatCategoryList", cacheManager = "categoryListCacheManager")
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

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {"categoryList", "flatCategoryList"},
            allEntries = true,
            cacheManager = "categoryListCacheManager"
    )
    public void createCategory(CreateCategoryRequest request) {
        String kdcCode = request.kdcCode();
        Optional<Category> existingCategory = categoryRepository.findByKdcCode(kdcCode);

        if (existingCategory.isPresent()) {
            Category category = existingCategory.get();
            if (category.getCategoryName() != null) {
                throw new CategoryAlreadyExistsException(kdcCode);
            }
            category.updateCategoryName(request.categoryName());
            return;
        }

        Category parent = null;
        String parentCode = getParentKdcCode(kdcCode);
        String path;
        int depth;

        if (parentCode != null) {
            parent = categoryRepository.findByKdcCode(parentCode)
                    .filter(p -> p.getCategoryName() != null)
                    .orElseThrow(() -> new ParentCategoryNotFoundException(parentCode));
            path = parent.getPath() + "/" + trimTrailingZeros(kdcCode);
            depth = parent.getDepth() + 1;
        } else {
            path = "/" + trimTrailingZeros(kdcCode);
            depth = 1;
        }

        Category newCategory = Category.builder()
                .kdcCode(kdcCode)
                .categoryName(request.categoryName())
                .parentCategory(parent)
                .path(path)
                .depth(depth)
                .build();

        categoryRepository.save(newCategory);
    }

    private String getParentKdcCode(String kdcCode) {
        if (!kdcCode.matches("^[0-9]{3}$")) {
            return null;
        }

        if (kdcCode.endsWith("00")) {
            return null;
        } else if (kdcCode.endsWith("0")) {
            return kdcCode.substring(0, 1) + "00";
        } else {
            if (kdcCode.charAt(1) == '0') {
                return kdcCode.substring(0, 1) + "00";
            }
            return kdcCode.substring(0, 2) + "0";
        }
    }

    private String trimTrailingZeros(String kdcCode) {
        return kdcCode.replaceAll("0+$", "");
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {"categoryList", "flatCategoryList"},
            allEntries = true,
            cacheManager = "categoryListCacheManager"
    )
    public void updateCategory(Long categoryId, UpdateCategoryRequest request) {
        Category category = getCategory(categoryId);
        category.updateCategoryName(request.categoryName());
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {"categoryList", "flatCategoryList"},
            allEntries = true,
            cacheManager = "categoryListCacheManager"
    )
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        if ("UNC".equals(category.getKdcCode())) {
            throw new CategoryCannotDeleteException("미분류(UNC) 카테고리는 삭제할 수 없습니다.");
        }

        if (categoryRepository.existsByParentCategoryId(categoryId)) {
            throw new CategoryCannotDeleteException("하위 카테고리가 존재하여 삭제할 수 없습니다.");
        }

        if (bookRepository.existsByCategoryId(categoryId)) {
            throw new CategoryCannotDeleteException("해당 카테고리에 등록된 도서가 있어 삭제할 수 없습니다.");
        }

        if (discountPolicyRepository.existsByCategoryId(categoryId)) {
            throw new CategoryCannotDeleteException("해당 카테고리에 적용된 할인 정책이 있어 삭제할 수 없습니다.");
        }

        categoryRepository.deleteById(categoryId);
    }
}
