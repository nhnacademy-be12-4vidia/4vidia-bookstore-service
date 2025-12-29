package com.nhnacademy._vidiabookstoreservice.admin.service.impl;

import com.nhnacademy._vidiabookstoreservice.admin.dto.request.GradePolicyUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.GradePolicyResponse;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.exception.notfound.GradeNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.repository.GradeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class GradePolicyServiceImplTest {

    @Mock
    private GradeRepository gradeRepository;

    @InjectMocks
    private GradePolicyServiceImpl gradePolicyService;

    @Test
    @DisplayName("특정 등급 정책 조회 테스트 성공")
    void get_Success() {
        Grade grade1 = new Grade(1L, GradeName.WELCOME,1);

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade1));

        GradePolicyResponse response = gradePolicyService.get(1L);

        assertThat(response).isNotNull();
        assertThat(response.gradeId()).isEqualTo(1L);
        assertThat(response.gradeName()).isEqualTo(GradeName.WELCOME.toString());
        assertThat(response.pointRate()).isEqualTo(1);
    }

    @Test
    @DisplayName("특정 등급 정책 조회 테스트 실패 - 존재하지 않는 정책")
    void get_Fail_NotFound() {
        when(gradeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gradePolicyService.get(99L))
                .isInstanceOf(GradeNotFoundException.class);

        verify(gradeRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("모든 등급 정책 조회 테스트")
    void getAllTest(){
        //given
        Grade grade1 = new Grade(GradeName.WELCOME,1);
        Grade grade2 = new Grade(GradeName.GOLD,3);
        when(gradeRepository.findAll()).thenReturn(List.of(grade1,grade2));

        //when
        List<GradePolicyResponse> responses = gradePolicyService.getAll();

        //then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).gradeName()).isEqualTo("WELCOME");
        assertThat(responses.get(1).gradeName()).isEqualTo("GOLD");
        verify(gradeRepository,times(1)).findAll();
    }

    @Test
    @DisplayName("등급 정책 수정 성공 테스트")
    void updateSuccessTest() {
        // given
        Long gradeId = 1L;
        Integer newPointRate = 5;
        Grade grade = new Grade(gradeId, GradeName.WELCOME, 1);
        GradePolicyUpdateRequest updateRequest = new GradePolicyUpdateRequest(newPointRate);

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));

        // when
        gradePolicyService.update(gradeId, updateRequest);

        // then
        // 도메인 모델(Grade) 내부에서 변경이 일어났는지 확인 (Dirty Checking 방식)
        assertThat(grade.getPointRate()).isEqualTo(newPointRate);
        verify(gradeRepository, times(1)).findById(gradeId);
    }

    @Test
    @DisplayName("등급 정책 수정 실패 - 등급을 찾을 수 없는 경우")
    void updateFailNotFoundTest() {
        // given
        Long gradeId = 999L;
        GradePolicyUpdateRequest updateRequest = new GradePolicyUpdateRequest(5);
        when(gradeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> gradePolicyService.update(gradeId, updateRequest))
                .isInstanceOf(GradeNotFoundException.class);

        verify(gradeRepository, times(1)).findById(gradeId);
    }



}