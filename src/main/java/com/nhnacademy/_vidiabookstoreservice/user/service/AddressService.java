package com.nhnacademy._vidiabookstoreservice.user.service;


import com.nhnacademy._vidiabookstoreservice.user.dto.request.AddressRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.request.CreateAddressRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {
    AddressResponse createAddress(Long userId, CreateAddressRequest request);
    AddressResponse getAddress(Long userId, Long addressId);
    List<AddressResponse> getUserAddresses(Long userId);
    AddressResponse updateAddress(Long id, Long addressId, AddressRequest request);
    void deleteAddress(Long id, Long addressId);
    void updateDefaultAddress(Long id, Long addressId);
    AddressResponse getDefaultAddress(Long userId);
}
