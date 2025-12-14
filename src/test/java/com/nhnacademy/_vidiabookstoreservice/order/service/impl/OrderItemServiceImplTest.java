package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderItemRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderItemResponse;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderItemNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceImplTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    @Test
    @DisplayName("주문 항목 추가 성공")
    void addOrderItem() {
        OrderItem orderItem = createOrderItem(null, ConfirmStatus.UNCONFIRMED);
        given(orderItemRepository.save(any(OrderItem.class))).willReturn(orderItem);

        OrderItem result = orderItemService.addOrderItem(orderItem);

        assertThat(result).isNotNull()
                .isEqualTo(orderItem);
        verify(orderItemRepository).save(any(OrderItem.class));
    }

    @Test
    @DisplayName("주문 항목 ID로 조회 성공")
    void getByOrderItemId_success() {
        Long orderItemId = 1L;
        OrderItem orderItem = createOrderItem(orderItemId, ConfirmStatus.UNCONFIRMED);
        given(orderItemRepository.findByOrderItemId(orderItemId)).willReturn(Optional.of(orderItem));

        OrderItemResponse result = orderItemService.getByOrderItemId(orderItemId);

        assertThat(result).usingRecursiveComparison()
                .isEqualTo(OrderItemResponse.from(orderItem));
    }

    @Test
    @DisplayName("ID로 프록시 객체 조회")
    void getProxyById() {
        Long orderItemId = 1L;
        OrderItem proxyOrderItem = OrderItem.builder().build();
        given(orderItemRepository.getReferenceById(orderItemId)).willReturn(proxyOrderItem);

        OrderItem result = orderItemService.getProxyById(orderItemId);

        assertThat(result).isEqualTo(proxyOrderItem);
    }

    @Test
    @DisplayName("주문상태 변경: 일반 변경 성공")
    void changeStatusOrderItem_success() {
        Long orderItemId = 1L;
        OrderItem orderItem = createOrderItem(orderItemId, ConfirmStatus.UNCONFIRMED);
        given(orderItemRepository.findByOrderItemId(orderItemId)).willReturn(Optional.of(orderItem));

        orderItemService.changeStatusOrderItem(orderItemId, ConfirmStatus.CONFIRMED);

        assertThat(orderItem.getConfirmStatus()).isEqualTo(ConfirmStatus.CONFIRMED);
    }

    @Test
    @DisplayName("상태 변경: 사용자 구매 확정 성공 (UNCONFIRMED -> CONFIRMED)")
    void changeStatusOrderItemByUser_success() {
        Long orderItemId = 2L;
        OrderItem orderItem = createOrderItem(orderItemId, ConfirmStatus.UNCONFIRMED);
        given(orderItemRepository.findByOrderItemId(orderItemId)).willReturn(Optional.of(orderItem));

        orderItemService.changeStatusOrderItem_byUser(orderItemId, ConfirmStatus.CONFIRMED);

        assertThat(orderItem.getConfirmStatus()).isEqualTo(ConfirmStatus.CONFIRMED);
    }

    @Test
    @DisplayName("상태 변경: 사용자 구매 확정 실패 (상태 불일치)")
    void changeStatusOrderItemByUser_fail_status_mismatch() {
        Long orderItemId = 2L;
        OrderItem orderItem = createOrderItem(orderItemId, ConfirmStatus.REFUND_REQUEST);
        given(orderItemRepository.findByOrderItemId(orderItemId)).willReturn(Optional.of(orderItem));

        orderItemService.changeStatusOrderItem_byUser(orderItemId, ConfirmStatus.CONFIRMED);

        assertThat(orderItem.getConfirmStatus()).isEqualTo(ConfirmStatus.REFUND_REQUEST);
    }

    @Test
    @DisplayName("예외: 존재하지 않는 ID로 조회 시 실패")
    void getByOrderItemId_fail_notFound() {
        given(orderItemRepository.findByOrderItemId(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderItemService.getByOrderItemId(99L))
                .isInstanceOf(OrderItemNotFoundException.class);
    }

    @Test
    @DisplayName("예외: 상태 변경 시 ID 없음 (기본)")
    void changeStatusOrderItem_fail_notFound() {
        given(orderItemRepository.findByOrderItemId(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderItemService.changeStatusOrderItem(99L, ConfirmStatus.CONFIRMED))
                .isInstanceOf(OrderItemNotFoundException.class);
    }

    @Test
    @DisplayName("예외: 상태 변경 시 ID 없음 (User)")
    void changeStatusOrderItemByUser_fail_notFound() {
        given(orderItemRepository.findByOrderItemId(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderItemService.changeStatusOrderItem_byUser(99L, ConfirmStatus.CONFIRMED))
                .isInstanceOf(OrderItemNotFoundException.class);
    }

    @Test
    @DisplayName("Order로 OrderItemRequest 리스트 변환 성공")
    void getOrderItemRequests_success() {
        Order mockOrder = Order.builder().build();
        Book mockBook = mock(Book.class);
        when(mockBook.getId()).thenReturn(1L);

        OrderItem item1 = OrderItem.builder()
                .order(mockOrder).book(mockBook).quantity(5).salePrice(20000)
                .confirmStatus(ConfirmStatus.UNCONFIRMED).build();
        OrderItem item2 = OrderItem.builder()
                .order(mockOrder).book(mockBook).quantity(1).salePrice(10000)
                .confirmStatus(ConfirmStatus.UNCONFIRMED).build();

        given(orderItemRepository.findByOrder(mockOrder)).willReturn(List.of(item1, item2));

        List<OrderItemRequest> result = orderItemService.getOrderItemRequests(mockOrder);

        assertThat(result).hasSize(2);
        assertThat(result).extracting("quantity").containsExactly(5, 1);
        assertThat(result).extracting("salePrice").containsExactly(20000, 10000);
    }

    @Test
    @DisplayName("Order로 조회 시 결과가 없으면 빈 리스트 반환")
    void getOrderItemRequests_returnsEmptyList() {
        Order mockOrder = Order.builder().build();
        given(orderItemRepository.findByOrder(mockOrder)).willReturn(Collections.emptyList());

        List<OrderItemRequest> result = orderItemService.getOrderItemRequests(mockOrder);

        assertThat(result).isEmpty();
    }


    private OrderItem createOrderItem(Long id, ConfirmStatus status) {
        OrderItem item = OrderItem.builder()
                .order(Order.builder().build())
                .book(Book.builder().build())
                .quantity(10)
                .salePrice(10000)
                .confirmStatus(status)
                .build();

        if (id != null) {
            item.setOrderItemId(id);
        }
        return item;
    }
}