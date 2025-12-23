package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentCancelRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentConfirmRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentFailRequest;
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
    @GetMapping("/{orderId}") // todo : {order-id}로 수정?
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable long orderId) {
        PaymentResponse paymentResponse = paymentService.getPayment(orderId);

        return ResponseEntity.status(HttpStatus.OK).body(paymentResponse);
    }

    /**
     * 결제 확정 및 저장
     * @param id : 주문아이디
     * @param confirmRequest : 결제 확정 요청 dto
     * @return
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> savePaymentDetail(@RequestParam long id,
                                                             @RequestBody PaymentConfirmRequest confirmRequest) {


        Long xUserId = UserContext.get().getUserId();
        Long xGuestId = UserContext.get().getGuestId();
        Long userId = xUserId == null ? xGuestId : xUserId; //비회원이면 장바구니 삭제시 필요함
        PaymentResponse paymentResponse = orderService.payAndCompleteOrder(id, confirmRequest, userId);

        return ResponseEntity.status(HttpStatus.OK).body(paymentResponse);
    }

    /**
     * 프론트 결제 처리 중 실패
     * @param paymentFailRequest : 실패한 주문 아이디
     * @return NO_CONTENT
     */
    @PostMapping("/rollback")
    public ResponseEntity<Void> rollbackPayment(@RequestBody PaymentFailRequest paymentFailRequest) {
        orderService.cancelOrder(paymentFailRequest.orderId(), "결제 중 실패");

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
