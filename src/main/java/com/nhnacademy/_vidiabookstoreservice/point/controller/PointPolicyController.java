package com.nhnacademy._vidiabookstoreservice.point.controller;

import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointPolicyUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.point.dto.response.PointPolicyResponse;
import com.nhnacademy._vidiabookstoreservice.point.service.impl.PointPolicyServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/point-policies")
public class PointPolicyController {

    private final PointPolicyServiceImpl service;

    @GetMapping
    public ResponseEntity<List<PointPolicyResponse>> getAll() {
        return ResponseEntity.ok().body(service.getAll());
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<PointPolicyResponse> get(@PathVariable Long policyId){
        return ResponseEntity.ok().body(service.get(policyId));
    }

    @PatchMapping("/{policyId}")
    public ResponseEntity<PointPolicyResponse> update(@PathVariable Long policyId,
                                      @Valid @RequestBody PointPolicyUpdateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.update(policyId, request));
    }

}
