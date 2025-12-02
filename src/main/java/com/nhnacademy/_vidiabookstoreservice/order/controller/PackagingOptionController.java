package com.nhnacademy._vidiabookstoreservice.order.controller;

import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.PackagingOptionRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.PackagingOptionResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.PackagingOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/packaging-options")
public class PackagingOptionController {
    private final PackagingOptionService packagingOptionService;

    @PostMapping
    public ResponseEntity<Void> createPackagingOption(@RequestBody PackagingOptionRequest packagingOptionRequest) {
        packagingOptionService.savePackagingOption(packagingOptionRequest);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<PackagingOptionResponse>> getAllPackagingOptions() {
        List<PackagingOptionResponse> packagingOptionResponses = packagingOptionService.getPackagingOptions();

        return ResponseEntity.status(HttpStatus.OK).body(packagingOptionResponses);
    }
}
