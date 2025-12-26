package com.nhnacademy._vidiabookstoreservice.admin.service;

import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminIsbnSearchResponse;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;

public interface AdminBookService {

    AdminIsbnSearchResponse processIsbnSearch(String isbn);

    AdminIsbnSearchResponse augmentBookInfo(String isbn);
    
    void evictIsbnCaches(String isbn);
}