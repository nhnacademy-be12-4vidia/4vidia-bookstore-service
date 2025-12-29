package com.nhnacademy._vidiabookstoreservice.book.service;

import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import com.nhnacademy._vidiabookstoreservice.book.dto.discountpolicy.DiscountPolicyCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.discountpolicy.DiscountPolicyResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.discountpolicy.DiscountPolicyUpdateRequest;
import java.util.List;

public interface DiscountPolicyService {

    Integer calculateSalesPrice(Integer priceStandard, Category category);

    List<DiscountPolicyResponse> getPolicies(Long categoryId);

    DiscountPolicyResponse getPolicy(Long id);

    void createPolicy(DiscountPolicyCreateRequest request);

    void updatePolicy(Long id, DiscountPolicyUpdateRequest request);

    void deletePolicy(Long id);
}
