package com.nhnacademy._vidiabookstoreservice.book.service;

import com.nhnacademy._vidiabookstoreservice.book.dto.category.response.CategoryListResponse;
import java.util.List;

public interface CategoryService {

    List<CategoryListResponse> getCategoryList();

}
