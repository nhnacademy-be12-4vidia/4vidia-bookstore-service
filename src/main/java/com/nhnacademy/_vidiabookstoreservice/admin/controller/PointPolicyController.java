package com.nhnacademy._vidiabookstoreservice.admin.controller;

import com.nhnacademy._vidiabookstoreservice.admin.dto.request.PointPolicyUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.PointPolicyResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.impl.PointPolicyServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/point-policies")
public class PointPolicyController {

    private final PointPolicyServiceImpl service;

    @GetMapping
    public ResponseEntity<List<PointPolicyResponse>> getAll() {
        return ResponseEntity.ok().body(service.getAll());
    }

    @GetMapping("/{policy-id}")
    public ResponseEntity<PointPolicyResponse> get(@PathVariable("policy-id") Long policyId){
        return ResponseEntity.ok().body(service.get(policyId));
    }

    @PutMapping("/{policy-id}")
    public ResponseEntity<Void> update(@PathVariable("policy-id") Long policyId,
                                      @Valid @RequestBody PointPolicyUpdateRequest request){
        service.update(policyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
