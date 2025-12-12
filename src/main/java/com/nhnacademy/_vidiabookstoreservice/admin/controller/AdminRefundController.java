package com.nhnacademy._vidiabookstoreservice.admin.controller;

import com.nhnacademy._vidiabookstoreservice.admin.dto.refund.response.AdminRefundListResponse;
import com.nhnacademy._vidiabookstoreservice.admin.dto.refund.response.RefundDetailResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.AdminRefundService;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
import com.nhnacademy._vidiabookstoreservice.refund.dto.RefundStatus;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/refunds")
@RequiredArgsConstructor
public class AdminRefundController {
    private final AdminRefundService adminRefundService;

    /**
     * 파손 반품 리스트
     */
    @GetMapping
    public ResponseEntity<PageResponse<AdminRefundListResponse>> listByRefundStatus(
            @RequestParam(value = "refundStatus", required = false, defaultValue = "PROCESS") RefundStatus refundStatus,
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable){
        Page<AdminRefundListResponse> refundList = adminRefundService.listByRefundStatus(refundStatus, keyword, pageable);
        return ResponseEntity.ok().body(PageResponse.from(refundList));
    }

    /**
     * 반품 상세 조회 (AJAX용)
     */
    @GetMapping("/{id}")
    public ResponseEntity<RefundDetailResponse> getRefundDetail(@PathVariable Long id) {
        RefundDetailResponse detail = adminRefundService.getRefundDetail(id);
        return ResponseEntity.ok(detail);
    }

    /**
     * 관리자 승인
     * POST /api/v1/order-service/admin/refunds/{id}/accept
     */
    @PostMapping("/{id}/accept")
    public ResponseEntity<Void> acceptRefund(@PathVariable Long id) {
        adminRefundService.acceptRefund(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 관리자 거절
     * POST /api/v1/order-service/admin/refunds/{id}/reject
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> rejectRefund(@PathVariable Long id) {
        adminRefundService.rejectRefund(id);
        return ResponseEntity.ok().build();
    }

}
