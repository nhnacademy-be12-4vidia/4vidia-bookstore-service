package com.nhnacademy._vidiabookstoreservice.admin.service.impl;


import com.nhnacademy._vidiabookstoreservice.admin.domain.PointPolicy;
import com.nhnacademy._vidiabookstoreservice.admin.dto.request.PointPolicyUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.PointPolicyResponse;
import com.nhnacademy._vidiabookstoreservice.admin.exception.PointPolicyNotFoundException;
import com.nhnacademy._vidiabookstoreservice.admin.repository.PointPolicyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PointPolicyServiceImplTest {

    @Mock
    PointPolicyRepository repository;

    @InjectMocks
    PointPolicyServiceImpl pointPolicyService;

    @Test
    @DisplayName("특정 포인트 정책 조회 성공")
    void get_Success() {
        PointPolicy policy1 = mock(PointPolicy.class);
        Long pointPolicyId = 1L;
        String pointName = "웰컴";
        Integer pointPrice = 5000;

        given(policy1.getPointPolicyId()).willReturn(pointPolicyId);
        given(policy1.getPointName()).willReturn(pointName);
        given(policy1.getPrice()).willReturn(pointPrice);

        when(repository.findById(1L)).thenReturn(Optional.of(policy1));

        PointPolicyResponse response = pointPolicyService.get(1L);

        assertThat(response).isNotNull();
        assertThat(response.pointPolicyId()).isEqualTo(pointPolicyId);
        assertThat(response.pointName()).isEqualTo(pointName);
        assertThat(response.price()).isEqualTo(pointPrice);
    }

    @Test
    @DisplayName("특정 포인트 정책 조회 테스트 실패 - 존재하지 않는 정책")
    void get_Fail_NotFound() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pointPolicyService.get(99L))
                .isInstanceOf(PointPolicyNotFoundException.class);

        verify(repository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("모든 포인트 정책 조회 테스트")
    void getAll(){
        PointPolicy policy1 = mock(PointPolicy.class);
        Long pointPolicyId1 = 1L;
        String pointName1 = "웰컴";
        Integer pointPrice1 = 5000;

        given(policy1.getPointPolicyId()).willReturn(pointPolicyId1);
        given(policy1.getPointName()).willReturn(pointName1);
        given(policy1.getPrice()).willReturn(pointPrice1);

        PointPolicy policy2 = mock(PointPolicy.class);
        Long pointPolicyId2 = 2L;
        String pointName2 = "리뷰";
        Integer pointPrice2 = 2000;

        given(policy2.getPointPolicyId()).willReturn(pointPolicyId2);
        given(policy2.getPointName()).willReturn(pointName2);
        given(policy2.getPrice()).willReturn(pointPrice2);

        when(repository.findAll()).thenReturn(List.of(policy1,policy2));

        List<PointPolicyResponse> responses = pointPolicyService.getAll();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).pointName()).isEqualTo(pointName1);
        assertThat(responses.get(1).pointName()).isEqualTo(pointName2);
        assertThat(responses.get(0).price()).isEqualTo(pointPrice1);
        assertThat(responses.get(1).price()).isEqualTo(pointPrice2);
        verify(repository,times(1)).findAll();
    }

    @Test
    @DisplayName("포인트 정책 수정 성공")
    void update_Success() {
        Long pointPolicyId = 1L;
        String pointName1 = "웰컴";
        Integer pointPrice1 = 3000;

        Integer newPointPrice = 5000;

        PointPolicy policy = createPointPolicy(pointPolicyId, pointName1, pointPrice1);

        when(repository.findById(pointPolicyId)).thenReturn(Optional.of(policy));

        PointPolicyUpdateRequest updateRequest = new PointPolicyUpdateRequest(newPointPrice);

        pointPolicyService.update(pointPolicyId, updateRequest);

        assertThat(policy.getPrice()).isEqualTo(newPointPrice);

        verify(repository, times(1)).findById(pointPolicyId);
    }

    @Test
    @DisplayName("포인트 정책 수정 실패 - 포인트 정책을 찾을 수 없는 경우")
    void update_Fail_NotFound() {
        PointPolicyUpdateRequest updateRequest = new PointPolicyUpdateRequest(5);

        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pointPolicyService.update(999L, updateRequest))
                .isInstanceOf(PointPolicyNotFoundException.class);

        verify(repository, times(1)).findById(anyLong());
    }


    // protect entity라 따로 생성
    private PointPolicy createPointPolicy(Long id, String name, Integer price) {
        try {
            Constructor<PointPolicy> constructor = PointPolicy.class.getDeclaredConstructor();

            constructor.setAccessible(true);

            PointPolicy policy = constructor.newInstance();

            ReflectionTestUtils.setField(policy, "pointPolicyId", id);
            ReflectionTestUtils.setField(policy, "pointName", name);
            ReflectionTestUtils.setField(policy, "price", price);

            return policy;
        } catch (Exception e) {
            throw new RuntimeException("PointPolicy 생성 실패", e);
        }
    }
}