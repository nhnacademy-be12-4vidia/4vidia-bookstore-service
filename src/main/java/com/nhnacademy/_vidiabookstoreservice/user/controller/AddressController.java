package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.user.dto.address.request.AddressRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.address.request.CreateAddressRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.address.response.AddressResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/my/addresses")
public class AddressController {

    private final AddressService addressService;

    /**
     * 주소 등록
     */
    @PostMapping
    public ResponseEntity<Void> registerAddress(@RequestHeader("X-User-Id") Long userId,
                                                  @Valid @RequestBody CreateAddressRequest createAddressRequest) {
        addressService.createAddress(userId, createAddressRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 Created
    }

    /**
     * 주소 단일 조회 -> 성공 확인 못함
     */
    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> getAddress(@RequestHeader("X-User-Id") Long userId,
                                                      @PathVariable Long addressId) {
        return ResponseEntity.ok().body(addressService.getAddress(userId, addressId)); // 200 OK + JSON
    }

    /**
     * 주소 전체 조회
     */
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAddressList(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok().body(addressService.getUserAddresses(userId)); // 200 OK + JSON
    }

    /**
     * 주소 수정
     */
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@RequestHeader("X-User-Id") Long userId,
                                                @PathVariable Long addressId,
                                                @Valid @RequestBody AddressRequest addressRequest) {
        // 수정한걸 꼭 리턴해야하나?
        return ResponseEntity.ok().body(addressService.updateAddress(userId, addressId, addressRequest));
    }

    /**
     * 주소삭제
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<String> deleteAddress(@RequestHeader("X-User-Id") Long userId,
                                                @PathVariable Long addressId) {
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok().body("delete"); // 204 No Content
    }


    /**
     * '기본'주소 변경
     */
    @PutMapping("/change-default/{addressId}")
    public ResponseEntity<Void> updateDefaultAddress(@RequestHeader("X-User-Id") Long userId,
                                                       @PathVariable Long addressId) {
        addressService.updateDefaultAddress(userId, addressId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * '기본'주소 조회
     */
    @GetMapping("/default")
    public ResponseEntity<AddressResponse> getDefaultAddress(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok().body(addressService.getDefaultAddress(userId)); // 200 OK + JSON
    }
}
