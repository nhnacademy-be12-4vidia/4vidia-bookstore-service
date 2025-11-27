package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.Packaging;
import com.nhnacademy._vidiabookstoreservice.order.domain.PackagingOption;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.OrderStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderResponse;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderItemRepository;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.order.repository.PackagingOptionRepository;
import com.nhnacademy._vidiabookstoreservice.order.repository.PackagingRepository;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PackagingRepository packagingRepository;
    private final PackagingOptionRepository packagingOptionRepository;

    public Long saveOrder(OrderCreateRequest request) {
        Long userId = 1L; //TODO userId 꺼내기. 비회원일 경우 랜덤값 생성?

        //TODO 재고 차감 구현 wow 어떻게하냐

        Order order = Order.builder()
                .userId(userId)
                .recipientName(request.recipientName())
                .addressRoadname(request.addressRoadname())
                .addressDetail(request.addressDetail())
                .zipCode(request.zipCode())
                .recipientPhone(request.recipientPhone())
                .deliveryRequest(request.deliveryRequest())
                .couponDiscount(request.couponDiscount())
                .pointUsed(request.pointUsed())
                .deliveryDate(request.deliveryDate())
                .totalPrice(request.totalPrice() + request.deliveryCost() + request.packagingCost())
                .payPrice(request.payPrice())
                .build();

        Order savedOrder = orderRepository.save(order);

        for (OrderCreateRequest.ItemRequestDto itemDto : request.orderItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .bookId(itemDto.bookId())
                    .quantity(itemDto.quantity())
                    .salePrice(itemDto.salePrice())
                    .confirmStatus(ConfirmStatus.UNCONFIRMED)
                    .build();

            savedOrder.getOrderItems().add(orderItem);

            OrderItem savedOrderItem = orderItemRepository.save(orderItem); //오류

            for (Long packagingOptionId : itemDto.packagingOptionIds()) {
                if (packagingOptionId != 0) {
                    PackagingOption packagingOption = packagingOptionRepository.findById(packagingOptionId)
                            .orElseThrow(() -> new NoSuchElementException("Packaging Option not found: " + packagingOptionId));


                    Packaging packaging = Packaging.builder()
                            .orderItem(savedOrderItem)
                            .packagingOption(packagingOption)
                            .build();

                    packagingRepository.save(packaging);
                }
            }


        }

        return savedOrder.getOrderId();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderResponse(Long orderId) {
        Order order = orderRepository.findByOrderId(orderId).orElseThrow(
                () -> new OrderNotFoundException("ID에 해당하는 주문내역을 찾을 수 없습니다. ID: %d".formatted(orderId))
        );

        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        Order order = orderRepository.findByOrderId(orderId).orElseThrow(
                () -> new OrderNotFoundException("ID에 해당하는 주문내역을 찾을 수 없습니다. ID: %d".formatted(orderId))
        );

        return order;
    }

    public void updateOrderStatus(Long orderId, OrderStatus orderStatus) {
        Order order = orderRepository.findByOrderId(orderId).orElseThrow(
                () -> new OrderNotFoundException("ID에 해당하는 주문내역을 찾을 수 없습니다. ID: %d".formatted(orderId))
        );

        order.setOrderStatus(orderStatus);
    }
}
