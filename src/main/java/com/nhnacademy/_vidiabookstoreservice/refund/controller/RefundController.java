package com.nhnacademy._vidiabookstoreservice.refund.controller;

import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import com.nhnacademy._vidiabookstoreservice.refund.dto.request.RefundRequest;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundHistoryResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundResponse;
import com.nhnacademy._vidiabookstoreservice.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RefundController {
    private final RefundService refundService;

    /**
     * 반품 신청 가능한 도서 리스트 불러오기
     */
    @GetMapping("/orders/{order-id}/refunds")
    public ResponseEntity<RefundResponse> getRefundList(@PathVariable("order-id") long orderId){
        RefundResponse response = refundService.getRefundList(orderId);
        return ResponseEntity.ok().body(response);
    }

    /**
     * 반품 신청서 작성
     */
    @PostMapping("/refunds")
    public ResponseEntity<Void> refundRegister(@RequestBody RefundRequest refundRequest){
        refundService.refundRegister(refundRequest);
        return ResponseEntity.ok().build();
    }

    /**
     * 반품 신청 내역 조회
     */
    @GetMapping("/users/me/refunds")
    public ResponseEntity<List<RefundHistoryResponse>> getMyRefundHistories(
            @RequestParam(value = "status", required = false) RefundStatus status
    ) {
        Long userId = UserContext.get().getUserId();

        List<RefundHistoryResponse> refundHistoryResponses = refundService.getMyRefunds(userId, status);
        return ResponseEntity.ok().body(refundHistoryResponses);
    }
}
