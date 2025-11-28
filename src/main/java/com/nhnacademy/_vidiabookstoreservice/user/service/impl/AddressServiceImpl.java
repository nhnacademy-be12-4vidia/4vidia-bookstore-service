package com.nhnacademy._vidiabookstoreservice.user.service.impl;


import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.UserAddress;
import com.nhnacademy._vidiabookstoreservice.user.dto.request.AddressRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.request.CreateAddressRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.response.AddressResponse;
import com.nhnacademy._vidiabookstoreservice.user.exception.AddressNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.exception.DefaultAddressCannotBeDeletedException;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.repository.AddressRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import com.nhnacademy._vidiabookstoreservice.user.service.AddressService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    // todo : 에러처리 - 서비스에서는 레포지토리 처리만 하고 컨트롤러에서는 받아오는거에 대해서만 처리

    /**
     * 주소 등록
     */
    @Override
    public AddressResponse createAddress(Long userId, CreateAddressRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("존재하지 않는 회원입니다. 회원pk : " + userId));

        // 주소 개수 제한
        int addrCount = addressRepository.countByUser_userId(userId);
        if(addrCount>=10){
            throw new IllegalArgumentException("주소는 최대 10개까지만 등록할 수 있습니다. 주소개수: " + addrCount);
        }

        UserAddress address = request.toEntity(user);

        addressRepository.save(address);
        return AddressResponse.fromEntity(address);
    }

    /**
     * 주소 단건 조회
     */
    @Override
    public AddressResponse getAddress(Long userId, Long addressId){
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("존재하지 않는 회원입니다. 회원pk : " + userId);
        }

        UserAddress userAddress = addressRepository.findByUser_UserIdAndUserAddressId(userId, addressId);
        if(userAddress == null){
            throw new IllegalArgumentException("해당 회원의 주소를 찾을 수 없습니다. 주소pk : " + addressId);
        }

        return AddressResponse.fromEntity(userAddress);
    }


    /**
     * 주소 전체 조회
     */
    @Override
    public List<AddressResponse> getUserAddresses(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("존재하지 않는 회원입니다. 회원pk : " + userId);
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
            throw new UserNotFoundException("존재하지 않는 회원입니다. 회원pk : " + userId);
        }

        UserAddress userAddress = addressRepository.findByUser_UserIdAndUserAddressId(userId, addressId);
        if (userAddress == null) {
            throw new AddressNotFoundException("해당 회원의 주소를 찾을 수 없습니다.");
        }

        // 기존 객체 업데이트
        userAddress.updateAddress(
                request.alias(),
                request.roadAddress(),
                request.zipCode(),
                request.addressDetail()
        );

        return AddressResponse.fromEntity(userAddress);
    }

    /**
     * 주소 삭제
     */
    @Override
    public void deleteAddress(Long userId, Long addressId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다. 회원pk : " + userId));

        // 주소가 등록되어있는지 체크
        UserAddress userAddress = addressRepository.findByUser_UserIdAndUserAddressId(userId, addressId);
        if (userAddress == null) {
            throw new AddressNotFoundException("해당 회원의 주소를 찾을 수 없습니다. 주소pk : " + addressId);
        }

        // 삭제하려고 하는 주소가 기본주소인지 체크
        UserAddress defaultAddress = user.getUserAddress();
        if (defaultAddress != null && defaultAddress.getUserAddressId().equals(userAddress.getUserAddressId())) {
            throw new DefaultAddressCannotBeDeletedException("기본 주소는 삭제할 수 없습니다. 기본 주소를 변경 후 시도해주세요. 현재 기본 주소 별칭 : " + defaultAddress.getAlias());
        }

        addressRepository.deleteByUser_UserIdAndUserAddressId(userId, addressId);
    }

    /**
     * 기본주소 변경
     */
    @Override
    public void updateDefaultAddress(Long userId, Long addressId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("존재하지 않는 회원입니다. 회원pk : " + userId));

        UserAddress selectedAddress = addressRepository.findByUser_UserIdAndUserAddressId(userId, addressId);
        if (selectedAddress == null) {
            throw new AddressNotFoundException("해당 회원의 주소를 찾을 수 없습니다. 주소pk : " + addressId);
        }

        // 변경하려고하는 주소가 -> 이미 기본주소로 등록되있는지 체크 (참고 - 프론트에서는 기본주소 옆에 체크박스 없애놨음)
        UserAddress currentDefaultAddress = user.getUserAddress();
        if (currentDefaultAddress != null && currentDefaultAddress.getUserAddressId().equals(addressId)) {
            return; // 이미 기본 주소이므로 정상 종료
        }

        user.setDefaultAddress(selectedAddress);
    }


    /**
     * 기본 주소 조회
     */
    @Override
    public AddressResponse getDefaultAddress(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("존재하지 않는 회원입니다. 회원pk : " + userId));

        UserAddress defaultAddress = user.getUserAddress();
        if (defaultAddress == null) {
            // todo : DefaultAddressNotFoundException 를 새로 만들어야하는가?
            throw new AddressNotFoundException("기본 주소가 설정되어 있지 않습니다.");
        }

        return AddressResponse.fromEntity(defaultAddress);
    }


}
