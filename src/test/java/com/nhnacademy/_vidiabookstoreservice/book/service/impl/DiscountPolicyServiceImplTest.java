package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import com.nhnacademy._vidiabookstoreservice.book.domain.DiscountPolicy;
import com.nhnacademy._vidiabookstoreservice.book.dto.discountpolicy.DiscountPolicyCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.discountpolicy.DiscountPolicyResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.discountpolicy.DiscountPolicyUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.book.exception.create.DiscountPolicyAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.CategoryNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.DiscountPolicyNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.mq.producer.DiscountPolicyProducer;
import com.nhnacademy._vidiabookstoreservice.book.repository.CategoryRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.DiscountPolicyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountPolicyServiceImplTest {

    @Mock
    private DiscountPolicyRepository discountPolicyRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private DiscountPolicyProducer discountPolicyProducer;

    @InjectMocks
    private DiscountPolicyServiceImpl discountPolicyService;

    @Test
    @DisplayName("할인 정책 생성 - 성공")
    void createPolicy_success() {
        DiscountPolicyCreateRequest request = mock(DiscountPolicyCreateRequest.class);
        Category category = mock(Category.class);
        DiscountPolicy policy = mock(DiscountPolicy.class);

        when(request.getCategoryId()).thenReturn(1L);
        when(discountPolicyRepository.existsByCategoryId(1L)).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(request.toEntity(category)).thenReturn(policy);

        discountPolicyService.createPolicy(request);

        verify(discountPolicyRepository).save(policy);
        verify(discountPolicyProducer).sendChangedEvent(1L, "CREATED");
    }

    @Test
    @DisplayName("할인 정책 생성 - 이미 존재할 경우 예외 발생")
    void createPolicy_alreadyExists_throwsException() {
        DiscountPolicyCreateRequest request = mock(DiscountPolicyCreateRequest.class);
        when(request.getCategoryId()).thenReturn(1L);
        when(discountPolicyRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThrows(DiscountPolicyAlreadyExistsException.class, () ->
                discountPolicyService.createPolicy(request));
    }

    @Test
    @DisplayName("할인 정책 생성 - 카테고리가 없는 전역 정책 생성 성공")
    void createPolicy_global_success() {
        DiscountPolicyCreateRequest request = mock(DiscountPolicyCreateRequest.class);
        when(request.getCategoryId()).thenReturn(null);
        when(discountPolicyRepository.existsByCategoryIsNull()).thenReturn(false);
        when(request.toEntity(null)).thenReturn(mock(DiscountPolicy.class));

        discountPolicyService.createPolicy(request);

        verify(discountPolicyRepository).save(any());
        verify(discountPolicyProducer).sendChangedEvent(null, "CREATED");
    }

    @Test
    @DisplayName("할인 정책 생성 - 전역 정책 이미 존재 시 예외")
    void createPolicy_globalAlreadyExists_throwsException() {
        DiscountPolicyCreateRequest request = mock(DiscountPolicyCreateRequest.class);
        when(request.getCategoryId()).thenReturn(null);
        when(discountPolicyRepository.existsByCategoryIsNull()).thenReturn(true);

        assertThrows(DiscountPolicyAlreadyExistsException.class, () -> discountPolicyService.createPolicy(request));
    }

    @Test
    @DisplayName("할인 정책 생성 - 존재하지 않는 카테고리에 할당 시 예외")
    void createPolicy_categoryNotFound_throwsException() {
        DiscountPolicyCreateRequest request = mock(DiscountPolicyCreateRequest.class);
        when(request.getCategoryId()).thenReturn(99L);
        when(discountPolicyRepository.existsByCategoryId(99L)).thenReturn(false);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> discountPolicyService.createPolicy(request));
    }

    @Test
    @DisplayName("할인 정책 수정 - 성공")
    void updatePolicy_success() {
        Long policyId = 1L;
        DiscountPolicyUpdateRequest request = mock(DiscountPolicyUpdateRequest.class);
        DiscountPolicy policy = mock(DiscountPolicy.class);
        Category category = mock(Category.class);

        when(discountPolicyRepository.findById(policyId)).thenReturn(Optional.of(policy));
        when(policy.getCategory()).thenReturn(category);
        when(category.getId()).thenReturn(10L);

        when(request.getDiscountPolicyName()).thenReturn("수정정책");
        when(request.getDiscountRate()).thenReturn(20);
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(10);
        when(request.getStartDate()).thenReturn(start);
        when(request.getEndDate()).thenReturn(end);

        discountPolicyService.updatePolicy(policyId, request);

        verify(policy).update("수정정책", 20, start, end);
        verify(discountPolicyProducer).sendChangedEvent(10L, "UPDATED");
    }

    @Test
    @DisplayName("할인 정책 삭제 - 성공")
    void deletePolicy_success() {
        Long policyId = 1L;
        DiscountPolicy policy = mock(DiscountPolicy.class);
        Category category = mock(Category.class);

        when(discountPolicyRepository.findById(policyId)).thenReturn(Optional.of(policy));
        when(policy.getCategory()).thenReturn(category);
        when(category.getId()).thenReturn(5L);

        discountPolicyService.deletePolicy(policyId);

        verify(discountPolicyRepository).deleteById(policyId);
        verify(discountPolicyProducer).sendChangedEvent(5L, "DELETED");
    }

    @Test
    @DisplayName("할인 정책 삭제 - 카테고리 있는 정책 삭제 시 MQ에 ID 전송")
    void deletePolicy_withCategory_sendsIdToMq() {
        Long id = 1L;
        DiscountPolicy policy = mock(DiscountPolicy.class);
        Category category = mock(Category.class);

        when(discountPolicyRepository.findById(id)).thenReturn(Optional.of(policy));
        when(policy.getCategory()).thenReturn(category);
        when(category.getId()).thenReturn(100L);

        discountPolicyService.deletePolicy(id);

        verify(discountPolicyProducer).sendChangedEvent(100L, "DELETED");
    }

    @Test
    @DisplayName("할인 정책 수정 - 전역 정책(카테고리 없음) 수정 시 MQ에 null 전송")
    void updatePolicy_noCategory_sendsNullToMq() {
        Long id = 1L;
        DiscountPolicy policy = mock(DiscountPolicy.class);
        DiscountPolicyUpdateRequest request = mock(DiscountPolicyUpdateRequest.class);

        when(discountPolicyRepository.findById(id)).thenReturn(Optional.of(policy));
        when(policy.getCategory()).thenReturn(null);

        discountPolicyService.updatePolicy(id, request);

        verify(discountPolicyProducer).sendChangedEvent(null, "UPDATED");
    }

    @Test
    @DisplayName("단건 정책 조회 - 실패 시 예외")
    void getPolicy_notFound_throwsException() {
        when(discountPolicyRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(DiscountPolicyNotFoundException.class, () -> discountPolicyService.getPolicy(1L));
    }

    @Test
    @DisplayName("특정 카테고리의 할인 정책 목록 조회 - 성공")
    void getPolicies_byCategoryId_success() {
        Long categoryId = 1L;
        DiscountPolicy policy = mock(DiscountPolicy.class);
        when(policy.getDiscountRate()).thenReturn(10);
        when(discountPolicyRepository.findByCategoryId(categoryId)).thenReturn(List.of(policy));

        List<DiscountPolicyResponse> result = discountPolicyService.getPolicies(categoryId);

        assertEquals(1, result.size());
        verify(discountPolicyRepository).findByCategoryId(categoryId);
    }

    @Test
    @DisplayName("단건 정책 상세 조회 - 성공")
    void getPolicy_success() {
        Long policyId = 1L;
        DiscountPolicy policy = mock(DiscountPolicy.class);
        when(policy.getDiscountRate()).thenReturn(15);
        when(discountPolicyRepository.findById(policyId)).thenReturn(Optional.of(policy));

        DiscountPolicyResponse result = discountPolicyService.getPolicy(policyId);

        assertNotNull(result);
        verify(discountPolicyRepository).findById(policyId);
    }

    @Test
    @DisplayName("할인 정책 수정 - 카테고리가 없는 정책(Global) 업데이트")
    void updatePolicy_global_success() {
        DiscountPolicy policy = mock(DiscountPolicy.class);
        DiscountPolicyUpdateRequest request = mock(DiscountPolicyUpdateRequest.class);
        when(discountPolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policy.getCategory()).thenReturn(null);

        discountPolicyService.updatePolicy(1L, request);

        verify(discountPolicyProducer).sendChangedEvent(null, "UPDATED");
    }

    @Test
    @DisplayName("할인 정책 삭제 - 카테고리가 없는 정책(Global) 삭제")
    void deletePolicy_global_success() {
        DiscountPolicy policy = mock(DiscountPolicy.class);
        when(discountPolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policy.getCategory()).thenReturn(null);

        discountPolicyService.deletePolicy(1L);

        verify(discountPolicyRepository).deleteById(1L);
        verify(discountPolicyProducer).sendChangedEvent(null, "DELETED");
    }

    @Test
    @DisplayName("판매가 계산 - 카테고리 할인 적용")
    void calculateSalesPrice_success() {
        Integer standardPrice = 10000;
        Category category = mock(Category.class);
        DiscountPolicy policy = mock(DiscountPolicy.class);

        when(category.getId()).thenReturn(1L);
        when(policy.getDiscountRate()).thenReturn(20); // 20% 할인
        when(discountPolicyRepository.findActivePolicyByCategoryId(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(policy));

        Integer salesPrice = discountPolicyService.calculateSalesPrice(standardPrice, category);

        assertEquals(8000, salesPrice);
    }

    @Test
    @DisplayName("판매가 계산 - 가격이 null이거나 0보다 작으면 0 반환")
    void calculateSalesPrice_invalidPrice_returnsZero() {
        assertEquals(0, discountPolicyService.calculateSalesPrice(null, mock(Category.class)));
        assertEquals(0, discountPolicyService.calculateSalesPrice(-100, mock(Category.class)));
    }

    @Test
    @DisplayName("판매가 계산 - 카테고리 정책이 없고 부모 정책이 있는 경우 (계층 탐색)")
    void calculateSalesPrice_parentCategoryPolicy_success() {
        Integer standardPrice = 10000;
        Category child = mock(Category.class);
        Category parent = mock(Category.class);
        DiscountPolicy parentPolicy = mock(DiscountPolicy.class);

        when(child.getId()).thenReturn(2L);
        when(child.getParentCategory()).thenReturn(parent);
        when(parent.getId()).thenReturn(1L);
        when(parentPolicy.getDiscountRate()).thenReturn(30);

        when(discountPolicyRepository.findActivePolicyByCategoryId(eq(2L), any())).thenReturn(Optional.empty());
        when(discountPolicyRepository.findActivePolicyByCategoryId(eq(1L), any())).thenReturn(Optional.of(parentPolicy));

        Integer salesPrice = discountPolicyService.calculateSalesPrice(standardPrice, child);

        assertEquals(7000, salesPrice);
    }

    @Test
    @DisplayName("판매가 계산 - 모든 카테고리에 정책이 없어 전역 정책 적용")
    void calculateSalesPrice_globalPolicy_success() {
        Integer standardPrice = 10000;
        Category category = mock(Category.class);
        DiscountPolicy globalPolicy = mock(DiscountPolicy.class);

        when(category.getId()).thenReturn(1L);
        when(globalPolicy.getDiscountRate()).thenReturn(15);

        when(discountPolicyRepository.findActivePolicyByCategoryId(anyLong(), any())).thenReturn(Optional.empty());
        when(discountPolicyRepository.findActiveGlobalPolicy(any())).thenReturn(Optional.of(globalPolicy));

        Integer salesPrice = discountPolicyService.calculateSalesPrice(standardPrice, category);

        assertEquals(8500, salesPrice);
    }

    @Test
    @DisplayName("판매가 계산 - 전역 정책조차 없어 기본값(10%) 적용")
    void calculateSalesPrice_defaultFallback_success() {
        Integer standardPrice = 10000;
        when(discountPolicyRepository.findActiveGlobalPolicy(any())).thenReturn(Optional.empty());

        Integer salesPrice = discountPolicyService.calculateSalesPrice(standardPrice, null);

        assertEquals(9000, salesPrice); // 10% 할인
    }

    @Test
    @DisplayName("판매가 계산 - 다단계 계층 탐색 (손자 -> 자식(X) -> 부모(O))")
    void calculateSalesPrice_multiLevelHierarchy_success() {
        Integer standardPrice = 20000;
        Category grandson = mock(Category.class);
        Category child = mock(Category.class);
        Category parent = mock(Category.class);
        DiscountPolicy parentPolicy = mock(DiscountPolicy.class);

        when(grandson.getId()).thenReturn(3L);
        when(grandson.getParentCategory()).thenReturn(child);

        when(child.getId()).thenReturn(2L);
        when(child.getParentCategory()).thenReturn(parent);

        when(parent.getId()).thenReturn(1L);
        when(parentPolicy.getDiscountRate()).thenReturn(50); // 50% 할인

        when(discountPolicyRepository.findActivePolicyByCategoryId(eq(3L), any())).thenReturn(Optional.empty());
        when(discountPolicyRepository.findActivePolicyByCategoryId(eq(2L), any())).thenReturn(Optional.empty());
        when(discountPolicyRepository.findActivePolicyByCategoryId(eq(1L), any())).thenReturn(Optional.of(parentPolicy));

        Integer salesPrice = discountPolicyService.calculateSalesPrice(standardPrice, grandson);

        assertEquals(10000, salesPrice);
    }

    @Test
    @DisplayName("모든 할인 정책 조회 - 리포지토리 호출 확인")
    void getPolicies_all_success() {
        DiscountPolicy policy = mock(DiscountPolicy.class);
        when(discountPolicyRepository.findAll()).thenReturn(List.of(policy));
        when(policy.getDiscountRate()).thenReturn(10);

        List<DiscountPolicyResponse> result = discountPolicyService.getPolicies(null);

        assertNotNull(result);
        verify(discountPolicyRepository).findAll();
    }
}