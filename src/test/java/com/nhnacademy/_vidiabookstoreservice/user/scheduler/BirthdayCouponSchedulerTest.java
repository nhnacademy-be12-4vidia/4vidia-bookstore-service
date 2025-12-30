package com.nhnacademy._vidiabookstoreservice.user.scheduler;

import com.nhnacademy._vidiabookstoreservice.global.client.CouponClient;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.dto.coupon.response.ActivePolicyIdResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.event.CouponIssueEvent;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BirthdayCouponSchedulerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private CouponClient couponClient;

    @InjectMocks
    private BirthdayCouponScheduler birthdayCouponScheduler;

    @Test
    @DisplayName("쿠폰 서비스 호출 실패 -> 스케줄러 스킵, 메시지 발행 안함")
    void issueBirthdayCoupons_couponClientThrows_skip() {
        // given
        when(couponClient.getActivePolicy("BIRTHDAY"))
                .thenThrow(new RuntimeException("coupon service down"));

        // when
        birthdayCouponScheduler.issueBirthdayCoupons();

        // then
        verifyNoInteractions(userRepository);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("활성 정책 응답이 2xx 아니거나 body null -> 발급 안 함")
    void issueBirthdayCoupons_policyNotAvailable_skip() {
        // case1) 2xx 아님
        when(couponClient.getActivePolicy("BIRTHDAY"))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.BAD_REQUEST));

        birthdayCouponScheduler.issueBirthdayCoupons();

        verifyNoInteractions(userRepository);
        verifyNoInteractions(rabbitTemplate);

        reset(couponClient, userRepository, rabbitTemplate);

        // case2) 2xx 인데 body null
        when(couponClient.getActivePolicy("BIRTHDAY"))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        birthdayCouponScheduler.issueBirthdayCoupons();

        verifyNoInteractions(userRepository);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("정상: 활성 정책 + 이번달 생일 유저들 -> 유저 수만큼 이벤트 발행")
    void issueBirthdayCoupons_success_publishEvents() {
        // given
        long policyId = 777L;

        // ActivePolicyIdResponse가 record라면 이렇게 생성 가능
        ActivePolicyIdResponse body = new ActivePolicyIdResponse(policyId);

        when(couponClient.getActivePolicy("BIRTHDAY"))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        int month = LocalDate.now().getMonthValue();

        User u1 = mock(User.class);
        User u2 = mock(User.class);
        when(u1.getUserId()).thenReturn(1L);
        when(u2.getUserId()).thenReturn(2L);

        when(userRepository.findBirthdayUsersByMonth(UserStatus.ACTIVE, month))
                .thenReturn(List.of(u1, u2));

        ArgumentCaptor<CouponIssueEvent> eventCaptor = ArgumentCaptor.forClass(CouponIssueEvent.class);

        // when
        birthdayCouponScheduler.issueBirthdayCoupons();

        // then - 이번달 생일 유저 조회
        verify(userRepository).findBirthdayUsersByMonth(UserStatus.ACTIVE, month);

        // then - 발행 2번
        verify(rabbitTemplate, times(2)).convertAndSend(
                eq("coupon4.exchange"),
                eq("coupon4.event.requested"),
                eventCaptor.capture()
        );

        List<CouponIssueEvent> events = eventCaptor.getAllValues();
        assertEquals(2, events.size());

        // 이벤트 내용 검증 (userId, policyId, issuedAt/expireAt)
        assertEquals(1L, events.get(0).userId());
        assertEquals(policyId, events.get(0).policyId());
        assertNotNull(events.get(0).issuedAt());
        assertNotNull(events.get(0).expireAt());
        assertEquals(Duration.ofDays(7),
                Duration.between(events.get(0).issuedAt(), events.get(0).expireAt()));

        assertEquals(2L, events.get(1).userId());
        assertEquals(policyId, events.get(1).policyId());
        assertEquals(Duration.ofDays(7),
                Duration.between(events.get(1).issuedAt(), events.get(1).expireAt()));
    }

    @Test
    @DisplayName("정상: 활성 정책은 있는데 이번달 생일 유저 0명 -> 이벤트 발행 안함")
    void issueBirthdayCoupons_success_noUsers_noPublish() {
        // given
        long policyId = 777L;
        ActivePolicyIdResponse body = new ActivePolicyIdResponse(policyId);

        when(couponClient.getActivePolicy("BIRTHDAY"))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        int month = LocalDate.now().getMonthValue();
        when(userRepository.findBirthdayUsersByMonth(UserStatus.ACTIVE, month))
                .thenReturn(List.of());

        // when
        birthdayCouponScheduler.issueBirthdayCoupons();

        // then
        verify(userRepository).findBirthdayUsersByMonth(UserStatus.ACTIVE, month);
        verifyNoInteractions(rabbitTemplate);
    }
}
