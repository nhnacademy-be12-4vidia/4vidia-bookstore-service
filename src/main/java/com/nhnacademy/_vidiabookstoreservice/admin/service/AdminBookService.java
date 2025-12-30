package com.nhnacademy._vidiabookstoreservice.admin.service;

import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminIsbnSearchResponse;

public interface AdminBookService {

    AdminIsbnSearchResponse processIsbnSearch(String isbn);

    AdminIsbnSearchResponse augmentBookInfo(String isbn);
    
    void evictIsbnCaches(String isbn);
}