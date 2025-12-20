
package com.nhnacademy._vidiabookstoreservice.user.service.event;

import com.nhnacademy._vidiabookstoreservice.global.client.CouponClient;
import com.nhnacademy._vidiabookstoreservice.user.dto.event.WelcomeCouponIssueEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WelcomeCouponEventListenerTest {

    @Mock
    private CouponClient couponClient;

    @InjectMocks
    private WelcomeCouponEventListener listener;

    @Test
    @DisplayName("회원가입 이벤트 수신 시 웰컴쿠폰 발급 요청을 호출한다")
    void handleWelcomeCoupon_success_callsCouponClient() {
        // given
        WelcomeCouponIssueEvent event = new WelcomeCouponIssueEvent(1L);

        // when
        listener.handleWelcomeCoupon(event);

        // then
        verify(couponClient, times(1)).getRegisterCoupon(1L);
    }

    @Test
    @DisplayName("쿠폰 발급 요청에서 예외가 발생해도 예외를 던지지 않고 삼킨다(로그만 남김)")
    void handleWelcomeCoupon_fail_swallowException() {
        // given
        WelcomeCouponIssueEvent event = new WelcomeCouponIssueEvent(1L);
        doThrow(new RuntimeException("boom"))
                .when(couponClient).getRegisterCoupon(1L);

        // when + then (예외가 밖으로 터지면 테스트 실패)
        listener.handleWelcomeCoupon(event);

        // 쿠폰 호출 시도는 했는지 검증
        verify(couponClient, times(1)).getRegisterCoupon(1L);
    }
}
