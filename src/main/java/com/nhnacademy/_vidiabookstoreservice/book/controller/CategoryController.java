package com.nhnacademy._vidiabookstoreservice.book.controller;

import com.nhnacademy._vidiabookstoreservice.book.dto.category.response.CategoryListResponse;
import com.nhnacademy._vidiabookstoreservice.book.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryListResponse>> getCategoryList() {
        return ResponseEntity.ok(categoryService.getCategoryList());
    }

    @GetMapping("/flat")
    public ResponseEntity<List<CategoryListResponse>> getFlatCategoryList() {
        return ResponseEntity.ok(categoryService.getFlatCategoryList());
    }
}
