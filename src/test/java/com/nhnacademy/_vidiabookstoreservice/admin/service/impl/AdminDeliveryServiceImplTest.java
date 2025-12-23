package com.nhnacademy._vidiabookstoreservice.admin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.nhnacademy._vidiabookstoreservice.admin.exception.DeliveryEndInvalidException;
import com.nhnacademy._vidiabookstoreservice.admin.exception.DeliveryStartInvalidException;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import com.nhnacademy._vidiabookstoreservice.order.exception.notfound.OrderNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminDeliveryServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private AdminDeliveryServiceImpl adminDeliveryService;

    /**
     * Order 엔티티 생성을 위한 헬퍼 메서드
     */
    private Order createTestOrder(Long id, DeliveryStatus status) {
        // 서비스 로직에서 User의 내부 필드(Email 등)를 직접 쓰지 않으므로 단순 mock 객체만 연결
        User mockUser = mock(User.class);

        Order order = Order.builder()
                .user(mockUser)
                .recipientName("테스트")
                .addressRoadname("광주광역시")
                .addressDetail("NHN 아카데미")
                .zipCode("12345")
                .recipientPhone("010-1234-5678")
                .totalBookPrice(20000)
                .packagingFee(0)
                .deliveryFee(3000)
                .deliveryDate(LocalDate.now().plusDays(2))
                .build();

        ReflectionTestUtils.setField(order, "orderId", id);
        ReflectionTestUtils.setField(order, "deliveryStatus", status);
        ReflectionTestUtils.setField(order, "orderItems", new ArrayList<>());

        return order;
    }

    @Test
    @DisplayName("배송 목록 조회 - 검색어 트리밍 검증")
    void listByDeliveryStatus_Success() {
        String keyword = "  테스트  ";
        Pageable pageable = PageRequest.of(0, 10);
        Order order = createTestOrder(1L, DeliveryStatus.WAITING);
        Page<Order> page = new PageImpl<>(List.of(order));

        given(orderRepository.searchAdminDeliveries(DeliveryStatus.WAITING, "테스트", pageable))
                .willReturn(page);

        Page<Order> result = adminDeliveryService.listByDeliveryStatus(DeliveryStatus.WAITING, keyword, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).searchAdminDeliveries(DeliveryStatus.WAITING, "테스트", pageable);
    }

    @Test
    @DisplayName("배송 시작 - 상태 WAITING에서 시작 시 성공")
    void startDelivery_Success() {
        Long orderId = 10L;
        Order order = createTestOrder(orderId, DeliveryStatus.WAITING);
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        Order result = adminDeliveryService.startDelivery(orderId);

        assertThat(result.getDeliveryStatus()).isEqualTo(DeliveryStatus.SHIPPING);
        assertThat(result.getActualDeliveryDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("배송 시작 실패 - 이미 배송 중인 경우 예외 발생")
    void startDelivery_Fail_InvalidStatus() {
        Long orderId = 10L;
        Order order = createTestOrder(orderId, DeliveryStatus.SHIPPING);
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> adminDeliveryService.startDelivery(orderId))
                .isInstanceOf(DeliveryStartInvalidException.class);
    }

    @Test
    @DisplayName("배송 완료 - SHIPPING 상태에서 완료 시 성공")
    void completeDelivery_Success() {
        Long orderId = 20L;
        Order order = createTestOrder(orderId, DeliveryStatus.SHIPPING);
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        Order result = adminDeliveryService.completeDelivery(orderId);

        assertThat(result.getDeliveryStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    @DisplayName("배송 완료 실패 - 배송 시작 전(WAITING) 완료 처리 시 예외 발생")
    void completeDelivery_Fail_InvalidStatus() {
        Long orderId = 20L;
        Order order = createTestOrder(orderId, DeliveryStatus.WAITING);
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> adminDeliveryService.completeDelivery(orderId))
                .isInstanceOf(DeliveryEndInvalidException.class);
    }

    @Test
    @DisplayName("주문 존재하지 않음 - 예외 발생")
    void delivery_OrderNotFound() {
        given(orderRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminDeliveryService.startDelivery(99L))
                .isInstanceOf(OrderNotFoundException.class);
    }
}