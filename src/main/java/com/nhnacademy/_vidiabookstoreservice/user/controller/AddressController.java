package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
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
@RequestMapping("/users/me/addresses") // 기존 "/my/addresses"
public class AddressController {

    private final AddressService addressService;

    /**
     * 주소 등록
     */
    @PostMapping
    public ResponseEntity<Void> registerAddress(@Valid @RequestBody CreateAddressRequest createAddressRequest) {
        Long userId = UserContext.get().getUserId();
        addressService.createAddress(userId, createAddressRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 Created
    }

    /**
     * 주소 단일 조회
     */
    @GetMapping("/{address-id}")
    public ResponseEntity<AddressResponse> getAddress(@PathVariable("address-id") Long addressId) {
        Long userId = UserContext.get().getUserId();

        return ResponseEntity.ok().body(addressService.getAddress(userId, addressId)); // 200 OK + JSON
    }

    /**
     * 주소 전체 조회
     */
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAddressList() {
        Long userId = UserContext.get().getUserId();
        return ResponseEntity.ok().body(addressService.getUserAddresses(userId)); // 200 OK + JSON
    }

    /**
     * 기본주소 조회
     */
    @GetMapping("/default")
    public ResponseEntity<AddressResponse> getDefaultAddress() {
        Long userId = UserContext.get().getUserId();

        return ResponseEntity.ok().body(addressService.getDefaultAddress(userId)); // 200 OK + JSON
    }

    /**
     * 주소 수정
     */
    @PutMapping("/{address-id}")
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable("address-id") Long addressId,
                                                         @Valid @RequestBody AddressRequest addressRequest) {
        Long userId = UserContext.get().getUserId();
        return ResponseEntity.ok().body(addressService.updateAddress(userId, addressId, addressRequest));
    }

    /**
     * 기본주소 변경
     * 기존 "/change-default/{addressId}"
     */
    @PutMapping("/{address-id}/default")
    public ResponseEntity<Void> updateDefaultAddress(@PathVariable("address-id") Long addressId) {
        Long userId = UserContext.get().getUserId();
        addressService.updateDefaultAddress(userId, addressId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * 주소 삭제
     */
    @DeleteMapping("/{address-id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable("address-id") Long addressId) {
        Long userId = UserContext.get().getUserId();
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok().build(); // 204 No Content
    }
}
