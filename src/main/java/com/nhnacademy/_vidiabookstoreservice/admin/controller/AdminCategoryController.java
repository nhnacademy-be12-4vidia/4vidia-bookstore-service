package com.nhnacademy._vidiabookstoreservice.admin.controller;

import com.nhnacademy._vidiabookstoreservice.book.dto.category.request.CreateCategoryRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.category.request.UpdateCategoryRequest;
import com.nhnacademy._vidiabookstoreservice.book.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Void> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<Void> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long categoryId
    ) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok().build();
    }
}
