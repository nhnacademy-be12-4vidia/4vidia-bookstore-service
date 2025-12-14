package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.nhnacademy._vidiabookstoreservice.order.domain.PackagingOption;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.PackagingOptionRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.PackagingOptionResponse;
import com.nhnacademy._vidiabookstoreservice.order.exception.PackagingOptionNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.repository.PackagingOptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackagingOptionServiceImplTest {

    @Mock
    private PackagingOptionRepository packagingOptionRepository;

    @InjectMocks
    private PackagingOptionServiceImpl packagingOptionService;

    @Test
    @DisplayName("포장 옵션 저장 성공")
    void savePackagingOption() {
        String expectName = "포장지 이름";
        int expectPrice = 1000;

        PackagingOptionRequest mockRequest = mock(PackagingOptionRequest.class);
        when(mockRequest.name()).thenReturn(expectName);
        when(mockRequest.price()).thenReturn(expectPrice);

        PackagingOption expectOption = createPackagingOption(null, expectName, expectPrice);

        given(packagingOptionRepository.save(any(PackagingOption.class))).willReturn(expectOption);

        PackagingOption result = packagingOptionService.savePackagingOption(mockRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(expectName);
        assertThat(result.getPrice()).isEqualTo(expectPrice);

        verify(packagingOptionRepository).save(any(PackagingOption.class));
    }

    @Test
    @DisplayName("포장 옵션 종류 전체 조회 성공 - 리스트 존재")
    void getPackagingOptions_success() {
        PackagingOption option1 = createPackagingOption(1L, "포장지 1", 1000);
        PackagingOption option2 = createPackagingOption(2L, "포장지 2", 2000);

        given(packagingOptionRepository.findAll()).willReturn(List.of(option1, option2));

        List<PackagingOptionResponse> resultList = packagingOptionService.getPackagingOptions();

        assertThat(resultList).hasSize(2);

        // AssertJ의 extracting을 사용하면 Response 객체를 일일이 만들지 않아도 필드값 검증 가능
        assertThat(resultList).extracting("name")
                .containsExactly("포장지 1", "포장지 2");
        assertThat(resultList).extracting("price")
                .containsExactly(1000, 2000);

        verify(packagingOptionRepository).findAll();
    }

    @Test
    @DisplayName("포장 옵션 종류 전체 조회 성공 - 빈 리스트")
    void getPackagingOptions_returnsEmptyList() {
        given(packagingOptionRepository.findAll()).willReturn(Collections.emptyList());

        List<PackagingOptionResponse> resultList = packagingOptionService.getPackagingOptions();

        assertThat(resultList).isEmpty();
        verify(packagingOptionRepository).findAll();
    }

    @Test
    @DisplayName("ID로 포장 옵션 조회 성공")
    void getByPackagingOptionId_success() {
        Long packagingOptionId = 1L;
        PackagingOption expect = createPackagingOption(packagingOptionId, "포장지 이름", 1000);

        given(packagingOptionRepository.findByPackagingOptionId(packagingOptionId)).willReturn(Optional.of(expect));

        PackagingOption result = packagingOptionService.getByPackagingOptionId(packagingOptionId);

        assertThat(result).usingRecursiveComparison()
                .isEqualTo(expect);
    }

    @Test
    @DisplayName("예외: ID로 포장 옵션 조회 실패 - 존재하지 않는 ID")
    void getByPackagingOptionId_fail() {
        given(packagingOptionRepository.findByPackagingOptionId(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> packagingOptionService.getByPackagingOptionId(99L))
                .isInstanceOf(PackagingOptionNotFoundException.class);
    }


    private PackagingOption createPackagingOption(Long id, String name, int price) {
        PackagingOption option = PackagingOption.builder()
                .name(name)
                .price(price)
                .build();

        if (id != null) {
            option.setPackagingOptionId(id);
        }
        return option;
    }
}