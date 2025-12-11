package com.nhnacademy._vidiabookstoreservice.refund.service.impl;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderItemNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderItemRepository;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.refund.domain.Refund;
import com.nhnacademy._vidiabookstoreservice.refund.dto.RefundStatus;
import com.nhnacademy._vidiabookstoreservice.refund.dto.request.RefundRequest;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.OrderItemResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundResponse;
import com.nhnacademy._vidiabookstoreservice.refund.repository.RefundRepository;
import com.nhnacademy._vidiabookstoreservice.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;

    @Override
    @Transactional(readOnly = true)
    public RefundResponse getRefundList(long orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        List<OrderItem> orderItems =  orderItemRepository.findAllByConfirmStatusAndOrder(ConfirmStatus.UNCONFIRMED,order);

        List<OrderItemResponse> response = orderItems.stream().map(OrderItemResponse::from).toList();

        return new RefundResponse(orderId, response);
    }

    @Override
    public void refundRegister(RefundRequest refundRequest) {
        // 주문 조회
        Order order = orderRepository.findByOrderId(refundRequest.orderId())
                .orElseThrow(() -> new OrderNotFoundException(refundRequest.orderId()));

        LocalDate deliveryStart = order.getActualDeliveryDate();
        List<Long> itemIds = refundRequest.orderItemIds();

        // 단순 변심 반품 : 배송일 확인
        if (!refundRequest.damaged()) {
            // 배송일 ChronoUnit이용 -> 두 LocalDate 객체 사이 일수 계산
            long daysSinceDelivery = ChronoUnit.DAYS.between(deliveryStart, LocalDate.now());

            if (daysSinceDelivery > 10) {
                for (Long itemId : itemIds) {
                    OrderItem orderItem = orderItemRepository.findById(itemId)
                            .orElseThrow(() -> new OrderItemNotFoundException(itemId));

                    Refund refund = Refund.builder()
                            .orderItem(orderItem)
                            .damaged(false)
                            .description(refundRequest.reason()) // 이거 없애고 싶다
                            .refundStatus(RefundStatus.REJECT)
                            .build();

                    refundRepository.save(refund);
                }
            } else { // 자동 수락
                for(Long itemId : itemIds){
                    OrderItem orderItem = orderItemRepository.findById(itemId)
                            .orElseThrow(() -> new OrderItemNotFoundException(itemId));

                    Refund refund = Refund.builder()
                            .orderItem(orderItem)
                            .damaged(false)
                            .description(refundRequest.reason())
                            .refundStatus(RefundStatus.ACCEPT)
                            .build();

                    refundRepository.save(refund);

                    //OrderItem 상태 변경
                    orderItem.setConfirmStatus(ConfirmStatus.REFUNDED);
                    orderItemRepository.save(orderItem);

                    //TODO 포인트 + 환불금액 포인트 적립
                }
            }
        }else{
            // 파손 -> 관리자
            for(Long itemId : itemIds){
                OrderItem orderItem = orderItemRepository.findById(itemId)
                        .orElseThrow(() -> new OrderItemNotFoundException(itemId));

                Refund refund = Refund.builder()
                        .orderItem(orderItem)
                        .damaged(true)
                        .description(refundRequest.reason())
                        .refundStatus(RefundStatus.PROCESS) // PROCESS(0)로 가정
                        .build();
                refundRepository.save(refund);

                //OrderItem 상태 변경
                orderItem.setConfirmStatus(ConfirmStatus.REFUND_REQUEST);
                orderItemRepository.save(orderItem);
            }
        }
    }
}
