package com.nhnacademy._vidiabookstoreservice.user.scheduler;

import com.nhnacademy._vidiabookstoreservice.user.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserDormantSchedulerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserDormantScheduler scheduler;

    @Test
    @DisplayName("markDormantUsers() 호출 시, 3개월 전 기준으로 AuthService.convertDormantUsers를 호출한다")
    void markDormantUsers_callsAuthServiceWithThreeMonthsAgo() {
        // given
        given(authService.convertDormantUsers(org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .willReturn(3);

        LocalDateTime beforeCall = LocalDateTime.now();

        // when
        scheduler.markDormantUsers();

        // then
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(authService).convertDormantUsers(captor.capture());

        LocalDateTime threshold = captor.getValue();

        // "호출 시각" 기준으로 threshold가 약 3개월 전인지 검증 (약간의 오차 허용)
        LocalDateTime expected = beforeCall.minusMonths(3);

        Duration diff = Duration.between(expected, threshold).abs();
        assertThat(diff.getSeconds()).isLessThan(5); // 5초 이내면 OK(환경에 따라 2~10초로 조절)
    }
}
