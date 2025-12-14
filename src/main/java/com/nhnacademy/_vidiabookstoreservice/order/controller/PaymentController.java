package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentCancelRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentConfirmRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.TossPaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import com.nhnacademy._vidiabookstoreservice.order.service.PaymentService;
import com.nhnacademy._vidiabookstoreservice.order.service.impl.TossPaymentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService<TossPaymentResponse> paymentService;
    private final OrderService orderService;

    // 결제 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable long orderId) {
        PaymentResponse paymentResponse = paymentService.getPayment(orderId);

        return ResponseEntity.status(HttpStatus.OK).body(paymentResponse);
    }

//    @PostMapping("/{orderId}/cancel") //배송 전 전액취소 상황
//    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable long orderId,
//                                                         @RequestBody PaymentCancelRequest cancelRequest) {
//
//        TossPaymentResponse tossPaymentResponse = paymentService.cancelPayment(cancelRequest.paymentKey(), cancelRequest.reason(), cancelRequest.amount());
//
//        Order order = orderService.getOrder(orderId);
//
//        PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.from(order, tossPaymentResponse, tossPaymentResponse.cancels().getLast().cancelAmount());
//
//        PaymentResponse paymentResponse = paymentService.savePayment(paymentCreateRequest);
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(paymentResponse);
//    }

    /**
     * 결제 확정 및 저장
     * @param xUserId : 회원아이디 or null
     * @param xGuestId : 비회원아이디 or null
     * @param id : 주문아이디
     * @param confirmRequest : 결제 확정 요청 dto
     * @return
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> savePaymentDetail(@RequestHeader(value = "X-User-Id", required = false) Long xUserId,
                                                             @RequestHeader(value = "X-Guest-Id", required = false) Long xGuestId,
                                                             @RequestParam long id,
                                                             @RequestBody PaymentConfirmRequest confirmRequest) {

        Long userId = xUserId == null ? xGuestId : xUserId; //비회원이면 장바구니 삭제시 필요함
        PaymentResponse paymentResponse = orderService.payAndCompleteOrder(id, confirmRequest, userId);

        return ResponseEntity.status(HttpStatus.OK).body(paymentResponse);
    }

}
