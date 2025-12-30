package com.nhnacademy._vidiabookstoreservice.user.scheduler;

import com.nhnacademy._vidiabookstoreservice.user.service.GradeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeUpdateSchedulerTest {

    @Mock
    private GradeService gradeService;

    @InjectMocks
    private GradeUpdateScheduler gradeUpdateScheduler;

    @Test
    @DisplayName("updateUserGrade: 월간 등급 재산정 호출 1번 수행")
    void updateUserGrade_callsRecalculateMonthlyGradesOnce() {
        // given
        when(gradeService.recalculateMonthlyGrades()).thenReturn(123);

        // when
        gradeUpdateScheduler.updateUserGrade();

        // then
        verify(gradeService, times(1)).recalculateMonthlyGrades();
        verifyNoMoreInteractions(gradeService);
    }

    @Test
    @DisplayName("updateUserGrade: gradeService가 예외 던지면 그대로 전파(현재 코드 기준)")
    void updateUserGrade_propagatesException() {
        // given
        when(gradeService.recalculateMonthlyGrades()).thenThrow(new RuntimeException("boom"));

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> gradeUpdateScheduler.updateUserGrade());
    }
}
