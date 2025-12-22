package com.nhnacademy._vidiabookstoreservice.admin.controller;

import com.nhnacademy._vidiabookstoreservice.admin.dto.refund.AdminRefundListResponse;
import com.nhnacademy._vidiabookstoreservice.admin.dto.refund.RefundDetailResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.AdminRefundService;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import com.nhnacademy._vidiabookstoreservice.refund.dto.request.RefundItemUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminRefundController {
    private final AdminRefundService adminRefundService;

    /**
     * 파손 반품 리스트
     */
    @GetMapping("/refunds")
    public ResponseEntity<PageResponse<AdminRefundListResponse>> listByRefundStatus(
            @RequestParam(value = "refundStatus", required = false, defaultValue = "PROCESS") RefundStatus refundStatus,
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable){
        Page<AdminRefundListResponse> refundList = adminRefundService.listByRefundStatus(refundStatus, keyword, pageable);
        return ResponseEntity.ok().body(PageResponse.from(refundList));
    }

    /**
     * 관리자 페이지 반품 상세 조회
     */
    @GetMapping("/refunds/{refund-id}")
    public ResponseEntity<RefundDetailResponse> getRefundDetail(@PathVariable("refund-id") Long refundId) {
        RefundDetailResponse detail = adminRefundService.getRefundDetail(refundId);
        return ResponseEntity.ok(detail);
    }

    /**
     * 관리자 반품 승인
     */
    @PutMapping("/refunds/{refund-item-id}")
    public ResponseEntity<Void> updateRefund(@PathVariable("refund-item-id") Long refundItemId,
                                             @RequestBody RefundItemUpdateRequest request) {

        adminRefundService.updateRefundStatus(refundItemId, request);
        return ResponseEntity.noContent().build();
    }
}
