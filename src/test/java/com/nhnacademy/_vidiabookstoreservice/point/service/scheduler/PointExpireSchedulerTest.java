package com.nhnacademy._vidiabookstoreservice.point.service.scheduler;

import com.nhnacademy._vidiabookstoreservice.point.domain.PointDetail;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import com.nhnacademy._vidiabookstoreservice.point.repository.PointDetailRepository;
import com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointExpireSchedulerTest {

    @Mock
    PointDetailRepository pointDetailRepository;

    @Mock
    PointCommandService pointCommandService;

    @InjectMocks
    PointExpireScheduler pointExpireScheduler;

    @Test
    @DisplayName("만료 대상 포인트가 없으면 expirePoints를 호출하지 않는다")
    void expirePoints_emptyList_noServiceCall() {
        when(pointDetailRepository.findExpiredPoints(any(LocalDate.class), eq(PointReason.POINT_EXPIRE)))
                .thenReturn(List.of());
        pointExpireScheduler.expirePoints();
        verify(pointDetailRepository, times(1))
                .findExpiredPoints(any(LocalDate.class), eq(PointReason.POINT_EXPIRE));
        verifyNoInteractions(pointCommandService);
    }

    @Test
    @DisplayName("만료 대상 포인트가 있으면 각 포인트에 대해 expirePoints를 호출한다")
    void expirePoints_hasList_callForEach() {
        PointDetail p1 = mock(PointDetail.class);
        PointDetail p2 = mock(PointDetail.class);
        PointDetail p3 = mock(PointDetail.class);

        when(pointDetailRepository.findExpiredPoints(any(LocalDate.class), eq(PointReason.POINT_EXPIRE)))
                .thenReturn(List.of(p1, p2, p3));

        pointExpireScheduler.expirePoints();

        verify(pointCommandService).expirePoints(p1);
        verify(pointCommandService).expirePoints(p2);
        verify(pointCommandService).expirePoints(p3);
        verify(pointCommandService, times(3)).expirePoints(any(PointDetail.class));
    }

    @Test
    @DisplayName("중간에 하나 실패해도 나머지는 계속 처리한다")
    void expirePoints_oneThrows_continue() {
        PointDetail p1 = mock(PointDetail.class);
        PointDetail p2 = mock(PointDetail.class);
        PointDetail p3 = mock(PointDetail.class);

        when(pointDetailRepository.findExpiredPoints(any(LocalDate.class), eq(PointReason.POINT_EXPIRE)))
                .thenReturn(List.of(p1, p2, p3));

        doThrow(new RuntimeException("boom"))
                .when(pointCommandService).expirePoints(p2);

        pointExpireScheduler.expirePoints();

        verify(pointCommandService).expirePoints(p1);
        verify(pointCommandService).expirePoints(p2);
        verify(pointCommandService).expirePoints(p3); // 예외 나도 계속 호출돼야 함
    }
}
