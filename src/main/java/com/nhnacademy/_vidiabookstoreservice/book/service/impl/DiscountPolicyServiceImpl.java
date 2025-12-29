package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import com.nhnacademy._vidiabookstoreservice.book.domain.DiscountPolicy;
import com.nhnacademy._vidiabookstoreservice.book.dto.discountpolicy.DiscountPolicyCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.discountpolicy.DiscountPolicyResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.discountpolicy.DiscountPolicyUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.book.exception.create.DiscountPolicyAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.CategoryNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.DiscountPolicyNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.CategoryRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.DiscountPolicyRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.DiscountPolicyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiscountPolicyServiceImpl implements DiscountPolicyService {

    private final DiscountPolicyRepository discountPolicyRepository;
    private final CategoryRepository categoryRepository;
    private static final int DEFAULT_DISCOUNT_RATE = 10; // 정책이 아예 없을 때 안전장치

    @Override
    @Transactional(readOnly = true)
    public Integer calculateSalesPrice(Integer priceStandard, Category category) {
        if (priceStandard == null || priceStandard < 0) {
            return 0;
        }

        int discountRate = findBestDiscountRate(category);
        
        // 정가 * (100 - 할인율) / 100
        long salesPrice = (long) priceStandard * (100 - discountRate) / 100;
        return (int) salesPrice;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountPolicyResponse> getPolicies(Long categoryId) {
        List<DiscountPolicy> policies;
        if (categoryId != null) {
            policies = discountPolicyRepository.findByCategoryId(categoryId);
        } else {
            policies = discountPolicyRepository.findAll();
        }
        return policies.stream()
            .map(DiscountPolicyResponse::fromEntity)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DiscountPolicyResponse getPolicy(Long id) {
        DiscountPolicy policy = discountPolicyRepository.findById(id)
            .orElseThrow(() -> new DiscountPolicyNotFoundException(id));
        return DiscountPolicyResponse.fromEntity(policy);
    }

    @Override
    @Transactional
    public void createPolicy(DiscountPolicyCreateRequest request) {
        if (request.getCategoryId() != null) {
            if (discountPolicyRepository.existsByCategoryId(request.getCategoryId())) {
                throw new DiscountPolicyAlreadyExistsException();
            }
        } else {
            if (discountPolicyRepository.existsByCategoryIsNull()) {
                throw new DiscountPolicyAlreadyExistsException();
            }
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId()));
        }

        DiscountPolicy policy = request.toEntity(category);
        discountPolicyRepository.save(policy);
    }

    @Override
    @Transactional
    public void updatePolicy(Long id, DiscountPolicyUpdateRequest request) {
        DiscountPolicy policy = discountPolicyRepository.findById(id)
            .orElseThrow(() -> new DiscountPolicyNotFoundException(id));

        policy.update(
            request.getDiscountPolicyName(),
            request.getDiscountRate(),
            request.getStartDate(),
            request.getEndDate()
        );
    }

    @Override
    @Transactional
    public void deletePolicy(Long id) {
        discountPolicyRepository.deleteById(id);
    }

    private int findBestDiscountRate(Category category) {
        LocalDate today = LocalDate.now();
        Category current = category;

        // 1. 카테고리 계층을 타고 올라가며 정책 탐색
        while (current != null) {
            // 해당 카테고리에 유효한 정책이 있는지 확인
            Optional<DiscountPolicy> policyOpt = discountPolicyRepository.findActivePolicyByCategoryId(current.getId(), today);
            
            if (policyOpt.isPresent()) {
                return policyOpt.get().getDiscountRate();
            }

            // 부모로 이동
            current = current.getParentCategory();
        }

        // 2. 계층 탐색 실패 시 전역(기본) 정책 확인 (Category IS NULL)
        return discountPolicyRepository.findActiveGlobalPolicy(today)
                .map(DiscountPolicy::getDiscountRate)
                .orElse(DEFAULT_DISCOUNT_RATE); // 3. 최후의 보루
    }
}
