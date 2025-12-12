package com.nhnacademy._vidiabookstoreservice.refund.controller;

import com.nhnacademy._vidiabookstoreservice.refund.dto.request.RefundRequest;
import com.nhnacademy._vidiabookstoreservice.refund.dto.response.RefundResponse;
import com.nhnacademy._vidiabookstoreservice.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RefundController {
    private final RefundService refundService;

    @GetMapping("/orders/{orderId}/refunds")
    public ResponseEntity<RefundResponse> getRefundList(@PathVariable long orderId){
        RefundResponse response = refundService.getRefundList(orderId);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/refunds")
    public ResponseEntity<Void> refundRegister(@RequestBody RefundRequest refundRequest){
        refundService.refundRegister(refundRequest);
        return ResponseEntity.ok().build();
    }
}
