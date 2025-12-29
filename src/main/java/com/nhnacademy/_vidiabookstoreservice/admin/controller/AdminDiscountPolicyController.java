package com.nhnacademy._vidiabookstoreservice.admin.controller;

import com.nhnacademy._vidiabookstoreservice.book.dto.discountpolicy.DiscountPolicyCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.discountpolicy.DiscountPolicyResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.discountpolicy.DiscountPolicyUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.book.service.DiscountPolicyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/books/discount-policies")
@RequiredArgsConstructor
public class AdminDiscountPolicyController {

    private final DiscountPolicyService discountPolicyService;

    @GetMapping
    public ResponseEntity<List<DiscountPolicyResponse>> getPolicies(
            @RequestParam(required = false) Long categoryId
    ) {
        return ResponseEntity.ok(discountPolicyService.getPolicies(categoryId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiscountPolicyResponse> getPolicy(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(discountPolicyService.getPolicy(id));
    }

    @PostMapping
    public ResponseEntity<Void> createPolicy(
            @RequestBody DiscountPolicyCreateRequest request
    ) {
        discountPolicyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePolicy(
            @PathVariable Long id,
            @RequestBody DiscountPolicyUpdateRequest request
    ) {
        discountPolicyService.updatePolicy(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolicy(
            @PathVariable Long id
    ) {
        discountPolicyService.deletePolicy(id);
        return ResponseEntity.ok().build();
    }
}
