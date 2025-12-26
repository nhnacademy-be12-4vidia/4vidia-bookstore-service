package com.nhnacademy._vidiabookstoreservice.book.service;

import com.nhnacademy._vidiabookstoreservice.book.domain.Category;

public interface DiscountPolicyService {

    Integer calculateSalesPrice(Integer priceStandard, Category category);
}
