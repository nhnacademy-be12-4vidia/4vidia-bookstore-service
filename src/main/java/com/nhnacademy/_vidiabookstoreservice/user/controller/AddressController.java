package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.user.dto.request.AddressRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.request.CreateAddressRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.response.AddressResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/my/addresses")
public class AddressController {

    private final AddressService addressService;

    /**
     * 주소 등록 o
     */
    @PostMapping
    public ResponseEntity<String> registerAddress(@RequestHeader("X-USER-ID") Long id,
                                                  @Valid @RequestBody CreateAddressRequest createAddressRequest) { // todo : @Valid 붙이기
        addressService.createAddress(id, createAddressRequest);
        return ResponseEntity.ok("주소 등록 완료");
    }

    /**
     * 주소 단일 조회 -> 성공 확인 못함
     */
    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> getAddress(@RequestHeader("X-USER-ID") Long id,
                                                      @PathVariable Long addressId) {
        return ResponseEntity.ok(addressService.getAddress(id,addressId));
    }

    /**
     * 주소 전체 조회 o
     */
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAddressList(@RequestHeader("X-USER-ID") Long id) {
        return ResponseEntity.ok(addressService.getUserAddresses(id));
    }

    /**
     * 주소 수정
     */
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@RequestHeader("X-USER-ID") Long id,
                                                @PathVariable Long addressId,
                                                @Valid @RequestBody AddressRequest addressRequest) {
        return ResponseEntity.ok(addressService.updateAddress(id, addressId, addressRequest));
    }

    /**
     * 주소삭제
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<String> deleteAddress(@RequestHeader("X-USER-ID") Long id,
                                                @PathVariable Long addressId) {
        addressService.deleteAddress(id, addressId);
        return ResponseEntity.ok("주소 삭제 완료");
    }


    /**
     * 기본주소 변경
     */
    @PutMapping("/change-default/{addressId}")
    public ResponseEntity<String> updateDefaultAddress(@RequestHeader("X-USER-ID") Long id,
                                                       @PathVariable Long addressId) {
        addressService.updateDefaultAddress(id, addressId);
        return ResponseEntity.ok("기본주소 변경 완료");
    }

    /**
     * 기본주소 조회
     */
    @GetMapping("/default") // 기본주소 조회는 GET /addresses/default 로 설계
    public ResponseEntity<AddressResponse> getDefaultAddress(@RequestHeader("X-USER-ID") Long id) {
        return ResponseEntity.ok(addressService.getDefaultAddress(id));
    }
}
