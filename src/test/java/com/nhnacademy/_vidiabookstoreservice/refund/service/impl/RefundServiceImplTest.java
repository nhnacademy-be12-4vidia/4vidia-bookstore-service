package com.nhnacademy._vidiabookstoreservice.refund.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderItemRepository;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.point.domain.PointRefundCommand;
import com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService;
import com.nhnacademy._vidiabookstoreservice.refund.domain.Refund;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundAmount;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import com.nhnacademy._vidiabookstoreservice.refund.dto.request.RefundRequest;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundResponse;
import com.nhnacademy._vidiabookstoreservice.refund.exception.SimpleRefundNotAvailableException;
import com.nhnacademy._vidiabookstoreservice.refund.exception.already.DamageRefundNotAvailableException;
import com.nhnacademy._vidiabookstoreservice.refund.repository.RefundRepository;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefundServiceImplTest {
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private RefundRepository refundRepository;
    @Mock private PointCommandService pointCommandService;
    @Mock private RefundCalculator refundCalculator;

    @Mock private Order order;
    @Mock private User user;

    @InjectMocks
    private RefundServiceImpl refundService;

    @Nested
    @DisplayName("1. 반품 가능 리스트 조회 (getRefundList)")
    class GetRefundListTest {
        @Test
        @DisplayName("1-1. 배송 후 10일 이내 : 단순 변심 반품 가능")
        void getRefundList_Within10Days() {
            when(orderRepository.findByOrderId(1L)).thenReturn(Optional.of(order));
            when(order.getActualDeliveryDate()).thenReturn(LocalDate.now().minusDays(5));
            when(orderItemRepository.findAllByOrderIdWithRefunds(1L)).thenReturn(List.of());

            RefundResponse response = refundService.getRefundList(1L);

            assertThat(response.canReturnByChangeOfMind()).isTrue();
        }

        @Test
        @DisplayName("1-2. 배송 후 10일 초과 : 단순 변심 반품이 불가능")
        void getRefundList_After10Days() {
            when(orderRepository.findByOrderId(1L)).thenReturn(Optional.of(order));
            when(order.getActualDeliveryDate()).thenReturn(LocalDate.now().minusDays(11));
            when(orderItemRepository.findAllByOrderIdWithRefunds(1L)).thenReturn(List.of());

            RefundResponse response = refundService.getRefundList(1L);

            assertThat(response.canReturnByChangeOfMind()).isFalse();
        }
    }

    @Nested
    @DisplayName("2. 반품 신청 등록 (refundRegister)")
    class RefundRegisterTest {

        @BeforeEach
        void innerSetUp() {
            // 모든 신청 등록에서 공통으로 발생하는 주문 조회만 정의
            when(orderRepository.findByOrderId(1L)).thenReturn(Optional.of(order));

            // 중요: validateRefundPeriod에서 날짜를 항상 쓰므로 공통으로 정의
            // Strict 모드에서도 validateRefundPeriod는 무조건 실행되므로 UnnecessaryStubbing이 발생하지 않습니다.
            when(order.getActualDeliveryDate()).thenReturn(LocalDate.now().minusDays(1));
        }

        @Nested
        @DisplayName("2-A. 단순 변심 (damaged = false)")
        class SimpleChangeTest {

            @Test
            @DisplayName("2-A-1. 단순 변심 신청 성공")
            void register_SimpleChange_Success() {
                when(order.getActualDeliveryDate()).thenReturn(LocalDate.now());

                OrderItem orderItem = mock(OrderItem.class);
                when(orderItemRepository.findById(100L)).thenReturn(Optional.of(orderItem));

                when(refundCalculator.calculate(eq(orderItem), anyBoolean(), eq(true)))
                        .thenReturn(new RefundAmount(1000, 9000));
                when(order.getUser()).thenReturn(user);
                when(user.getUserId()).thenReturn(50L);
                when(order.getOrderId()).thenReturn(1L);

                RefundRequest request = new RefundRequest(1L, "단순 변심", false, List.of(100L));

                refundService.refundRegister(request);

                verify(refundRepository).save(any(Refund.class));
                verify(pointCommandService).refundSimpleChange(any(PointRefundCommand.class), eq(50L));
            }

            @Test
            @DisplayName("2-A-2. 배송 후 10일 초과 시 예외 발생 (Strict 대응)")
            void register_SimpleChange_PeriodExceeded() {
                when(order.getActualDeliveryDate()).thenReturn(LocalDate.now().minusDays(11));

                RefundRequest request = new RefundRequest(1L, "단순 변심", false, List.of(100L));

                assertThatThrownBy(() -> refundService.refundRegister(request))
                        .isInstanceOf(SimpleRefundNotAvailableException.class);
            }
        }

        @Nested
        @DisplayName("2-B. 파손/불량 (damaged = true)")
        class DamagedRefundTest {

            @Test
            @DisplayName("2-B-1. 파손 신청 성공 (신청서만 저장)")
            void register_Damaged_Success() {
                OrderItem orderItem = mock(OrderItem.class);
                when(orderItemRepository.findById(100L)).thenReturn(Optional.of(orderItem));

                RefundRequest request = new RefundRequest(1L, "책이 파손됨", true, List.of(100L));

                refundService.refundRegister(request);

                verify(refundRepository).save(any(Refund.class));
                verifyNoInteractions(pointCommandService, refundCalculator);
            }

            @Test
            @DisplayName("2-B-2. 배송 후 30일 초과 시 파손 반품 예외 발생")
            void register_Damaged_PeriodExceeded() {
                when(order.getActualDeliveryDate()).thenReturn(LocalDate.now().minusDays(31));
                RefundRequest request = new RefundRequest(1L, "책이 파손됨", true, List.of(100L));

                assertThatThrownBy(() -> refundService.refundRegister(request))
                        .isInstanceOf(DamageRefundNotAvailableException.class);
            }
        }
    }

    @Test
    @DisplayName("3. 내 반품 개수 조회 - 성공 (Group By 쿼리 활용)")
    void getMyRefundCounts_Success() {
        Long userId = 1L;

        Object[] row1 = {RefundStatus.PROCESS, 5L};
        Object[] row2 = {RefundStatus.APPROVED, 3L};
        List<Object[]> mockResults = List.of(row1, row2);

        when(refundRepository.countByUserGroupByStatus(userId)).thenReturn(mockResults);

        var response = refundService.getMyRefundCounts(userId);

        // 총합: 5 (PROCESS) + 3 (APPROVED) = 8
        assertThat(response.total()).isEqualTo(8L);
        assertThat(response.process()).isEqualTo(5L);
        assertThat(response.approved()).isEqualTo(3L);

        verify(refundRepository, times(1)).countByUserGroupByStatus(userId);
    }
}