package com.nhnacademy._vidiabookstoreservice.admin.service.impl;

import com.nhnacademy._vidiabookstoreservice.admin.dto.refund.AdminRefundListResponse;
import com.nhnacademy._vidiabookstoreservice.admin.dto.refund.RefundDetailResponse;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookImage;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.point.domain.PointRefundCommand;
import com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService;
import com.nhnacademy._vidiabookstoreservice.refund.domain.Refund;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundAmount;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundItem;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundItemStatus;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import com.nhnacademy._vidiabookstoreservice.refund.dto.request.RefundItemUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.refund.exception.RefundStatusInvalidException;
import com.nhnacademy._vidiabookstoreservice.refund.repository.RefundItemRepository;
import com.nhnacademy._vidiabookstoreservice.refund.repository.RefundRepository;
import com.nhnacademy._vidiabookstoreservice.refund.service.impl.RefundCalculator;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminRefundServiceImplTest {

    @Mock
    RefundRepository refundRepository;
    @Mock
    RefundItemRepository refundItemRepository;
    @Mock
    RefundCalculator refundCalculator;
    @Mock
    PointCommandService pointCommandService;

    @InjectMocks
    AdminRefundServiceImpl adminRefundService;

    @Test
    @DisplayName("반품 리스트 조회 - 상태가 null일 때 전체 조회")
    void listByRefundStatus_All() {
        Refund refund = createRefund(createOrder(createUser()), RefundStatus.PROCESS);
        Page<Refund> refundPage = new PageImpl<>(List.of(refund));
        Pageable pageable = Pageable.unpaged();

        given(refundRepository.findAll(pageable)).willReturn(refundPage);

        Page<AdminRefundListResponse> result = adminRefundService.listByRefundStatus(null, null, pageable);

        assertThat(result).hasSize(1);
        verify(refundRepository).findAll(pageable);
    }

    @Test
    @DisplayName("반품 리스트 조회 - 특정 상태일 때 전체 조회")
    void listByRefundStatus_Filtered() {
        RefundStatus status = RefundStatus.APPROVED;

        Refund refund = createRefund(createOrder(createUser()), status);
        Page<Refund> refundPage = new PageImpl<>(List.of(refund));
        Pageable pageable = Pageable.unpaged();

        given(refundRepository.findAllByRefundStatus(status, pageable)).willReturn(refundPage);

        Page<AdminRefundListResponse> result = adminRefundService.listByRefundStatus(status, null, pageable);

        assertThat(result).hasSize(1);
        verify(refundRepository).findAllByRefundStatus(status, pageable);
    }


    @Test
    @DisplayName("반품 상세 조회 성공")
    void getRefundDetail_Success() {
        Long refundId = 1L;
        User user = createUser();
        Order order = createOrder(user);
        Refund refund = createRefund(order, RefundStatus.PROCESS);
        OrderItem orderItem = createOrderItemWithBook(order);
        RefundItem refundItem = createRefundItem(10L, refund, orderItem, RefundItemStatus.PROCESS);

        given(refundRepository.findById(refundId)).willReturn(Optional.of(refund));

        given(refundItemRepository.findAllByRefund_RefundId(refundId)).willReturn(List.of(refundItem));

        RefundDetailResponse response = adminRefundService.getRefundDetail(refundId);

        assertThat(response.email()).isEqualTo(user.getEmail());
        assertThat(response.items()).hasSize(1);
        verify(refundRepository).findById(refundId);
    }

    @Test
    @DisplayName("반품 승인 - 성공 및 포인트 환불 호출")
    void updateRefundStatus_Success() {
        Long refundItemId = 10L;
        User user = createUser();
        Order order = createOrder(user);
        Refund refund = createRefund(order, RefundStatus.PROCESS);
        OrderItem orderItem = createOrderItem(order);
        RefundItem refundItem = spy(createRefundItem(refundItemId, refund, orderItem, RefundItemStatus.PROCESS));

        RefundItemUpdateRequest request = new RefundItemUpdateRequest(RefundItemStatus.APPROVED, "파손");
        RefundAmount refundAmount = new RefundAmount(1000, 500);

        given(refundItemRepository.findById(refundItemId)).willReturn(Optional.of(refundItem));
        given(refundCalculator.calculate(any(), anyBoolean(), anyBoolean())).willReturn(refundAmount);

        given(refundItemRepository.existsByRefund_RefundIdAndRefundItemStatus(refund.getRefundId(), RefundItemStatus.PROCESS))
                .willReturn(false);

        adminRefundService.updateRefundStatus(refundItemId, request);

        verify(refundItem).accept();
        verify(refundItem).updateRefundPrice(1500);
        verify(pointCommandService).refundDamaged(any(PointRefundCommand.class), eq(user.getUserId()));
    }

    @Test
    @DisplayName("반품 거절 - 성공")
    void updateRefundStatus_Rejected() {
        Long refundItemId = 10L;
        User user = createUser();
        Order order = createOrder(user);
        Refund refund = createRefund(order, RefundStatus.PROCESS);
        OrderItem orderItem = createOrderItem(order);
        RefundItem refundItem = spy(createRefundItem(refundItemId, refund, orderItem, RefundItemStatus.PROCESS));

        String rejectReason = "파손 아님";
        RefundItemUpdateRequest request = new RefundItemUpdateRequest(RefundItemStatus.REJECTED, rejectReason);

        given(refundItemRepository.findById(refundItemId)).willReturn(Optional.of(refundItem));

        given(refundItemRepository.existsByRefund_RefundIdAndRefundItemStatus(refund.getRefundId(), RefundItemStatus.PROCESS))
                .willReturn(true);

        adminRefundService.updateRefundStatus(refundItemId, request);

        verify(refundItem).reject(rejectReason);
        verify(pointCommandService, never()).refundDamaged(any(), any());
    }

    @Test
    @DisplayName("반품 상태 변경 - 잘못된 상태 요청 시 예외")
    void updateRefundStatus_Invalid() {
        Long refundItemId = 1L;
        RefundItemUpdateRequest request = new RefundItemUpdateRequest(RefundItemStatus.PROCESS, null);

        assertThatThrownBy(() -> adminRefundService.updateRefundStatus(refundItemId, request))
                .isInstanceOf(RefundStatusInvalidException.class);
    }


    // --- Helper Methods ---
    private <T> T createEntity(Class<T> clazz, Object... fieldNameAndValues) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            T entity = constructor.newInstance();
            for (int i = 0; i < fieldNameAndValues.length; i += 2) {
                ReflectionTestUtils.setField(entity, (String) fieldNameAndValues[i], fieldNameAndValues[i + 1]);
            }
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Entity creation failed", e);
        }
    }

    private OrderItem createOrderItemWithBook(Order order) {
        BookImage bookImage = createEntity(BookImage.class,
                "id", 100L,
                "imageUrl", "http://test-image.com/sample.jpg",
                "displayOrder", 1
        );

        Book book = createEntity(Book.class,
                "id", 10L,
                "title", "테스트 책 제목",
                "priceStandard", 15000,
                "bookImageList", List.of(bookImage)
        );

        return createEntity(OrderItem.class,
                "orderItemId", 1L,
                "order", order,
                "book", book,
                "quantity", 2,
                "salePrice", 15000
        );
    }

    private User createUser() {
        return createEntity(User.class, "userId", 1L, "email", "test@test.com", "name", "Tester");
    }

    private Order createOrder(User user) {
        return createEntity(Order.class, "orderId", 1L, "user", user);
    }

    private OrderItem createOrderItem(Order order) {
        return createEntity(OrderItem.class, "orderItemId", 1L, "order", order);
    }

    private Refund createRefund(Order order, RefundStatus status) {
        return createEntity(Refund.class, "refundId", 1L, "order", order, "refundStatus", status, "description", "reason", "createdAt", LocalDateTime.now());
    }

    private RefundItem createRefundItem(Long id, Refund refund, OrderItem orderItem, RefundItemStatus status) {
        return createEntity(RefundItem.class, "refundItemId", id, "refund", refund, "orderItem", orderItem, "refundItemStatus", status);
    }

}