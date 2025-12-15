package com.nhnacademy._vidiabookstoreservice.refund.controller;

import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.refund.dto.RefundStatus;
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
    @GetMapping("/orders/{orderId}/refunds")
    public ResponseEntity<RefundResponse> getRefundList(@PathVariable long orderId){
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
    public List<RefundHistoryResponse> getMyRefundHistories(
            @RequestParam(value = "status", required = false) RefundStatus status
    ) {
        Long userId = UserContext.get().getUserId();

        return refundService.getMyRefunds(userId, status);
    }
}
