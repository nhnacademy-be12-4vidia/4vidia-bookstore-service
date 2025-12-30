package com.nhnacademy._vidiabookstoreservice.refund.controller;

import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import com.nhnacademy._vidiabookstoreservice.refund.dto.request.RefundRequest;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundCountResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundHistoryGroupResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundResponse;
import com.nhnacademy._vidiabookstoreservice.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<PageResponse<RefundHistoryGroupResponse>> getMyRefundHistories(
            @RequestParam(value = "status", required = false) RefundStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = UserContext.get().getUserId();

        Pageable pageable = PageRequest.of(page, size);
        Page<RefundHistoryGroupResponse> refundHistoryResponses = refundService.getMyRefunds(userId, status,pageable);
        return ResponseEntity.ok(PageResponse.from(refundHistoryResponses));
    }

    @GetMapping("/users/me/refunds/counts")
    public ResponseEntity<RefundCountResponse> getMyRefundCounts() {
        Long userId = UserContext.get().getUserId();
        return ResponseEntity.ok(refundService.getMyRefundCounts(userId));
    }

}
