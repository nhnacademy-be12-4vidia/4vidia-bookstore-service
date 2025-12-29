package com.nhnacademy._vidiabookstoreservice.admin.controller;

import com.nhnacademy._vidiabookstoreservice.admin.dto.request.GradePolicyUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.GradePolicyResponse;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.PointPolicyResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.GradePolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/grade-policies")
public class GradePolicyController {
    private final GradePolicyService gradePolicyService;

    @GetMapping
    public ResponseEntity<List<GradePolicyResponse>> getAll(){
       return ResponseEntity.ok().body(gradePolicyService.getAll());
    }

    @GetMapping("/{policy-id}")
    public ResponseEntity<GradePolicyResponse> get(@PathVariable("policy-id") Long policyId){
        return ResponseEntity.ok().body(gradePolicyService.get(policyId));
    }

    @PutMapping("/{gradeId}")
    public ResponseEntity<Void> update(@PathVariable Long gradeId,
                                       @Valid @RequestBody GradePolicyUpdateRequest request){
        gradePolicyService.update(gradeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
