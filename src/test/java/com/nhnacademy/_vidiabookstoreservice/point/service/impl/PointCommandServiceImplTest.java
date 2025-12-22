package com.nhnacademy._vidiabookstoreservice.point.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


import com.nhnacademy._vidiabookstoreservice.admin.repository.PointPolicyRepository;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.point.domain.PointDetail;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointUseRequest;
import com.nhnacademy._vidiabookstoreservice.point.exception.already.PointRewardAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.point.exception.invalid.PointNotEnoughException;
import com.nhnacademy._vidiabookstoreservice.point.repository.PointDetailRepository;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PointCommandServiceImplTest {

    @Mock
    private PointDetailRepository pointDetailRepository;
    @Mock
    private PointPolicyRepository pointPolicyRepository;
    @Mock
    private UserService userService;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PointCommandServiceImpl pointCommandService;

    @Test
    @DisplayName("구매 확정 시 적립 테스트 - 성공")
    void reward_success() {
        // given
        User user = mock(User.class);
        Grade grade = mock(Grade.class);
        Order order = mock(Order.class);

        when(order.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(1L);
        when(order.getOrderId()).thenReturn(100L);
        when(userService.getUserById(1L)).thenReturn(user);
        when(user.getGrade()).thenReturn(grade);
        when(grade.getPointRate()).thenReturn(5); // 5% 적립
        when(orderRepository.calculateNetOrderPrice(anyLong(), any())).thenReturn(10000);
        when(pointDetailRepository.existsByUserIdAndOrderIdAndReason(anyLong(), anyLong(), any())).thenReturn(false);

        // when
        pointCommandService.reward(order);

        // then
        ArgumentCaptor<PointDetail> captor = ArgumentCaptor.forClass(PointDetail.class);
        verify(pointDetailRepository).save(captor.capture());

        PointDetail saved = captor.getValue();
        assertEquals(PointReason.ORDER_REWARD, saved.getReason());
        assertEquals(100L, saved.getOrderId());
        assertEquals(1L, saved.getUserId());
        assertEquals(500, saved.getPrice());

    }

    @Test
    @DisplayName("구매 확정 시 중복 적립 방지 테스트 - 예외 발생")
    void reward_already_exists() {
        // given
        Order order = mock(Order.class);
        User user = mock(User.class);
        when(order.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(1L);
        when(order.getOrderId()).thenReturn(100L);
        when(pointDetailRepository.existsByUserIdAndOrderIdAndReason(1L, 100L, PointReason.ORDER_REWARD)).thenReturn(true);

        // when & then
        assertThrows(PointRewardAlreadyExistsException.class, () -> pointCommandService.reward(order));
    }

    @Test
    @DisplayName("포인트 사용 테스트 - 성공")
    void use_success() {
        // given
        Long userId = 1L;
        PointUseRequest request = new PointUseRequest(100L, 500);
        User user = mock(User.class);
        PointDetail detail = mock(PointDetail.class);

        when(pointDetailRepository.getRemainPoint(eq(userId), any(LocalDate.class))).thenReturn(1000);
        when(pointDetailRepository.findAvailablePointForUse(eq(userId), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(detail));
        when(detail.getRemainingPrice()).thenReturn(1000);
        when(userService.getUserById(userId)).thenReturn(user);

        // when
        pointCommandService.use(request, userId);

        // then
        verify(detail, times(1)).decrease(500);
        verify(user, times(1)).subtractPoint(500);
    }

    @Test
    @DisplayName("포인트 사용 테스트 - 잔액 부족 시 예외 발생")
    void use_not_enough_points() {
        // given
        Long userId = 1L;
        PointUseRequest request = new PointUseRequest(100L, 2000);
        when(pointDetailRepository.getRemainPoint(eq(userId), any(LocalDate.class))).thenReturn(1000);

        // when & then
        assertThrows(PointNotEnoughException.class, () -> pointCommandService.use(request, userId));

    }
}