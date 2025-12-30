package com.nhnacademy._vidiabookstoreservice.point.service.impl;

import com.nhnacademy._vidiabookstoreservice.admin.repository.PointPolicyRepository;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.point.domain.PointDetail;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointUseRequest;
import com.nhnacademy._vidiabookstoreservice.point.exception.already.PointRewardAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.point.exception.invalid.PointInvalidException;
import com.nhnacademy._vidiabookstoreservice.point.exception.invalid.PointNotEnoughException;
import com.nhnacademy._vidiabookstoreservice.point.repository.PointDetailRepository;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.exception.notfound.UserNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointCommandServiceImplTest {

    @Mock PointDetailRepository pointDetailRepository;
    @Mock PointPolicyRepository pointPolicyRepository;
    @Mock UserService userService;
    @Mock UserRepository userRepository;
    @Mock OrderRepository orderRepository;

    @InjectMocks PointCommandServiceImpl pointCommandService;

    // ---------- reward() ----------
    @Test
    @DisplayName("구매 확정 적립 - 성공: PointDetail 저장 + user.addPoint 호출")
    void reward_success() {
        // given
        User user = mock(User.class);
        Grade grade = mock(Grade.class);
        Order order = mock(Order.class);

        when(order.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(1L);
        when(order.getOrderId()).thenReturn(100L);

        when(pointDetailRepository.existsByUserIdAndOrderIdAndReason(1L, 100L, PointReason.ORDER_REWARD))
                .thenReturn(false);

        when(userService.getUserById(1L)).thenReturn(user);
        when(user.getGrade()).thenReturn(grade);
        when(grade.getPointRate()).thenReturn(5);

        when(orderRepository.calculateNetOrderPrice(100L, PointReason.ORDER_CANCEL_REFUND))
                .thenReturn(10_000);

        // when
        pointCommandService.reward(order);

        // then
        ArgumentCaptor<PointDetail> captor = ArgumentCaptor.forClass(PointDetail.class);
        verify(pointDetailRepository).save(captor.capture());
        PointDetail saved = captor.getValue();

        assertEquals(1L, saved.getUserId());
        assertEquals(100L, saved.getOrderId());
        assertEquals(PointReason.ORDER_REWARD, saved.getReason());
        assertEquals(500, saved.getPrice()); // 10000 * 5%
        verify(user).addPoint(500);

        verifyNoMoreInteractions(pointPolicyRepository); // reward는 정책 repo 안 씀
    }

    @Test
    @DisplayName("구매 확정 적립 - 중복 적립이면 예외 + 저장/포인트증가 없음")
    void reward_already_exists() {
        // given
        Order order = mock(Order.class);
        User user = mock(User.class);
        when(order.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(1L);
        when(order.getOrderId()).thenReturn(100L);

        when(pointDetailRepository.existsByUserIdAndOrderIdAndReason(1L, 100L, PointReason.ORDER_REWARD))
                .thenReturn(true);

        // when & then
        assertThrows(PointRewardAlreadyExistsException.class, () -> pointCommandService.reward(order));
        verify(pointDetailRepository, never()).save(any());
        verify(userService, never()).getUserById(anyLong());
    }

    @Test
    @DisplayName("구매 확정 적립 - 순수 주문금액 0이면 그냥 return (저장 없음)")
    void reward_realPrice_zero_then_return() {
        // given
        User user = mock(User.class);
        Order order = mock(Order.class);
        when(order.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(1L);
        when(order.getOrderId()).thenReturn(100L);

        when(pointDetailRepository.existsByUserIdAndOrderIdAndReason(1L, 100L, PointReason.ORDER_REWARD))
                .thenReturn(false);

        when(userService.getUserById(1L)).thenReturn(user);
        when(user.getGrade()).thenReturn(mock(Grade.class));
        when(orderRepository.calculateNetOrderPrice(100L, PointReason.ORDER_CANCEL_REFUND))
                .thenReturn(0);

        // when
        pointCommandService.reward(order);

        // then
        verify(pointDetailRepository, never()).save(any());
        verify(user, never()).addPoint(anyInt());
    }

    @Test
    @DisplayName("구매 확정 적립 - 순수 주문금액 음수면 예외")
    void reward_realPrice_negative_then_throw() {
        // given
        User user = mock(User.class);
        Order order = mock(Order.class);
        when(order.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(1L);
        when(order.getOrderId()).thenReturn(100L);

        when(pointDetailRepository.existsByUserIdAndOrderIdAndReason(1L, 100L, PointReason.ORDER_REWARD))
                .thenReturn(false);

        when(userService.getUserById(1L)).thenReturn(user);
        when(user.getGrade()).thenReturn(mock(Grade.class));
        when(orderRepository.calculateNetOrderPrice(100L, PointReason.ORDER_CANCEL_REFUND))
                .thenReturn(-1);

        // when & then
        assertThrows(PointInvalidException.class, () -> pointCommandService.reward(order));
        verify(pointDetailRepository, never()).save(any());
    }

    // ---------- use() ----------
    @Test
    @DisplayName("포인트 사용 - 성공: FIFO로 차감 + 사용 기록 1회 저장 + user.subtractPoint")
    void use_success_fifo_multi_details() {
        // given
        Long userId = 1L;
        PointUseRequest request = new PointUseRequest(100L, 700);

        User user = mock(User.class);
        when(userRepository.findByUserIdWithLock(userId)).thenReturn(Optional.of(user));
        when(pointDetailRepository.getRemainPoint(eq(userId), any(LocalDate.class))).thenReturn(1000);

        // FIFO 대상 2개
        PointDetail d1 = mock(PointDetail.class);
        PointDetail d2 = mock(PointDetail.class);
        when(d1.getRemainingPrice()).thenReturn(500);
        when(d2.getRemainingPrice()).thenReturn(500);

        when(pointDetailRepository.findAvailablePointForUse(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of(d1, d2));

        // when
        pointCommandService.use(request, userId);

        // then: FIFO 차감
        verify(d1).decrease(500);
        verify(d2).decrease(200);

        // 사용기록은 "한번만" 저장되어야 함
        ArgumentCaptor<PointDetail> saveCaptor = ArgumentCaptor.forClass(PointDetail.class);
        verify(pointDetailRepository, times(1)).save(saveCaptor.capture());
        PointDetail saved = saveCaptor.getValue();
        assertEquals(PointReason.ORDER_USE, saved.getReason());
        assertEquals(100L, saved.getOrderId());
        assertEquals(1L, saved.getUserId());


        assertEquals(-700, saved.getPrice());

        verify(user).subtractPoint(700);

        // 호출 흐름 최소 보장: 락 유저 먼저
        InOrder inOrder = inOrder(userRepository, pointDetailRepository);
        inOrder.verify(userRepository).findByUserIdWithLock(userId);
        inOrder.verify(pointDetailRepository).findAvailablePointForUse(eq(userId), any(LocalDate.class));
        inOrder.verify(pointDetailRepository).getRemainPoint(eq(userId), any(LocalDate.class));
    }


    @Test
    @DisplayName("포인트 사용 - 잔액 부족이면 예외 + 차감/저장 없음")
    void use_not_enough_points() {
        // given
        Long userId = 1L;
        PointUseRequest request = new PointUseRequest(100L, 2000);

        User user = mock(User.class);
        when(userRepository.findByUserIdWithLock(userId)).thenReturn(Optional.of(user));
        when(pointDetailRepository.getRemainPoint(eq(userId), any(LocalDate.class))).thenReturn(1000);

        // when & then
        assertThrows(PointNotEnoughException.class, () -> pointCommandService.use(request, userId));
        verify(pointDetailRepository, never()).save(any());
        verify(user, never()).subtractPoint(anyInt());
    }

    @Test
    @DisplayName("포인트 사용 - 유저 락 조회 실패면 예외")
    void use_user_not_found() {
        // given
        Long userId = 1L;
        PointUseRequest request = new PointUseRequest(100L, 100);
        when(userRepository.findByUserIdWithLock(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> pointCommandService.use(request, userId));
        verify(pointDetailRepository, never()).save(any());
    }
}
