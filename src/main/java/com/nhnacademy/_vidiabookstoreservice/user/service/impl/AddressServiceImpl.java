package com.nhnacademy._vidiabookstoreservice.user.service.impl;


import com.nhnacademy._vidiabookstoreservice.user.domain.Address;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.dto.address.request.AddressRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.address.request.CreateAddressRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.address.response.AddressResponse;
import com.nhnacademy._vidiabookstoreservice.user.exception.DefaultAddressDeletedException;
import com.nhnacademy._vidiabookstoreservice.user.exception.invalid.AddressLimitException;
import com.nhnacademy._vidiabookstoreservice.user.exception.notfound.AddressNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.exception.notfound.DefaultAddressNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.exception.notfound.UserNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.repository.AddressRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import com.nhnacademy._vidiabookstoreservice.user.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    /**
     * 주소 등록
     */
    @Override
    public AddressResponse createAddress(Long userId, CreateAddressRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));

        // 주소 개수 제한
        int addrCount = addressRepository.countByUser_userId(userId);
        if(addrCount>=10){
            throw new AddressLimitException();
        }

        Address address = request.toEntity(user);

        addressRepository.save(address);
        return AddressResponse.fromEntity(address);
    }

    /**
     * 주소 단건 조회
     */
    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddress(Long userId, Long addressId){
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        Address address = addressRepository.findByUser_UserIdAndAddressId(userId, addressId);
        if(address == null){
            throw new AddressNotFoundException(addressId);
        }

        return AddressResponse.fromEntity(address);
    }


    /**
     * 주소 전체 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        return addressRepository.findAllByUser_UserId(userId)
                .stream()
                .map(AddressResponse::fromEntity)
                .toList();
    }

    /**
     * 주소 수정
     */
    @Override
    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request){
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        Address address = addressRepository.findByUser_UserIdAndAddressId(userId, addressId);
        if (address == null) {
            throw new AddressNotFoundException(addressId);
        }

        // 기존 객체 업데이트
        address.updateAddress(
                request.alias(),
                request.roadAddress(),
                request.zipCode(),
                request.addressDetail()
        );

        return AddressResponse.fromEntity(address);
    }

    /**
     * 주소 삭제
     */
    @Override
    public void deleteAddress(Long userId, Long addressId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 주소가 등록되어있는지 체크
        Address address = addressRepository.findByUser_UserIdAndAddressId(userId, addressId);
        if (address == null) {
            throw new AddressNotFoundException(addressId);
        }

        // 삭제하려고 하는 주소가 기본주소인지 체크
        Address defaultAddress = user.getAddress();
        if (defaultAddress != null && defaultAddress.getAddressId().equals(address.getAddressId())) {
            throw new DefaultAddressDeletedException(defaultAddress.getAlias());
        }

        addressRepository.deleteByUser_UserIdAndAddressId(userId, addressId);
    }

    /**
     * 기본주소 변경
     */
    @Override
    public void updateDefaultAddress(Long userId, Long addressId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));

        Address selectedAddress = addressRepository.findByUser_UserIdAndAddressId(userId, addressId);
        if (selectedAddress == null) {
            throw new AddressNotFoundException(addressId);
        }

        // 변경하려고하는 주소가 -> 이미 기본주소로 등록되있는지 체크 (참고 - 프론트에서는 기본주소 옆에 체크박스 없애놨음)
        Address currentDefaultAddress = user.getAddress();
        if (currentDefaultAddress != null && currentDefaultAddress.getAddressId().equals(addressId)) {
            return; // 이미 기본 주소이므로 정상 종료
        }

        user.setDefaultAddress(selectedAddress);
    }


    /**
     * 기본 주소 조회
     */
    @Override
    @Transactional(readOnly = true)
    public AddressResponse getDefaultAddress(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));

        Address defaultAddress = user.getAddress();
        if (defaultAddress == null) {
            throw new DefaultAddressNotFoundException();
        }

        return AddressResponse.fromEntity(defaultAddress);
    }


}
