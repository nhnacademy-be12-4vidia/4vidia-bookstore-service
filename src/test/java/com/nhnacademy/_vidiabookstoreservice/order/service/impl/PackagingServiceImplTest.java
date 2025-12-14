package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.nhnacademy._vidiabookstoreservice.order.domain.Packaging;
import com.nhnacademy._vidiabookstoreservice.order.repository.PackagingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PackagingServiceImplTest {

    @Mock
    private PackagingRepository packagingRepository;

    @InjectMocks
    private PackagingServiceImpl packagingService;

    @Test
    @DisplayName("포장 내역 저장 성공")
    void addPackaging() {
        Packaging packaging = Packaging.builder().build();
        given(packagingRepository.save(any(Packaging.class))).willReturn(packaging);

        Packaging result = packagingService.addPackaging(packaging);

        assertThat(result).isNotNull()
                .isEqualTo(packaging);

        verify(packagingRepository).save(any(Packaging.class));
    }
}