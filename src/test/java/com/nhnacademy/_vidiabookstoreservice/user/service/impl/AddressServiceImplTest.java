package com.nhnacademy._vidiabookstoreservice.user.service.impl;

import com.nhnacademy._vidiabookstoreservice.user.domain.Address;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.dto.address.request.AddressRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.address.request.CreateAddressRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.address.response.AddressResponse;
import com.nhnacademy._vidiabookstoreservice.user.exception.*;
import com.nhnacademy._vidiabookstoreservice.user.repository.AddressRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock AddressRepository addressRepository;
    @InjectMocks AddressServiceImpl addressService;

    // --------------------
    // createAddress
    // --------------------
    @Test
    @DisplayName("주소 등록 성공")
    void createAddress_success() {
        Long userId = 1L;
        User user = mock(User.class);

        CreateAddressRequest request = mock(CreateAddressRequest.class);
        Address newAddress = addressIdOnly(10L);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(addressRepository.countByUser_userId(userId)).willReturn(0);
        given(request.toEntity(user)).willReturn(newAddress);

        AddressResponse result = addressService.createAddress(userId, request);

        assertThat(result).isNotNull();
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("주소 등록 실패 - 유저 없음")
    void createAddress_fail_userNotFound() {
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.createAddress(userId, mock(CreateAddressRequest.class)))
                .isInstanceOf(UserNotFoundByUserIdException.class);

        verify(addressRepository, never()).save(any());
    }

    @Test
    @DisplayName("주소 등록 실패 - 10개 제한")
    void createAddress_fail_overLimit() {
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.of(mock(User.class)));
        given(addressRepository.countByUser_userId(userId)).willReturn(10);

        assertThatThrownBy(() -> addressService.createAddress(userId, mock(CreateAddressRequest.class)))
                .isInstanceOf(MaxAddressLimitExceededException.class);
    }

    // --------------------
    // getAddress
    // --------------------
    @Test
    @DisplayName("주소 단건 조회 성공")
    void getAddress_success() {
        Long userId = 1L;
        Long addressId = 100L;
        Address address = addressIdOnly(addressId);

        given(userRepository.existsById(userId)).willReturn(true);
        given(addressRepository.findByUser_UserIdAndAddressId(userId, addressId)).willReturn(address);

        AddressResponse result = addressService.getAddress(userId, addressId);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("주소 단건 조회 실패 - 유저 없음")
    void getAddress_fail_userNotFound() {
        given(userRepository.existsById(1L)).willReturn(false);

        assertThatThrownBy(() -> addressService.getAddress(1L, 100L))
                .isInstanceOf(UserNotFoundByUserIdException.class);
    }

    @Test
    @DisplayName("주소 단건 조회 실패 - 주소 없음")
    void getAddress_fail_addressNotFound() {
        given(userRepository.existsById(1L)).willReturn(true);
        given(addressRepository.findByUser_UserIdAndAddressId(1L, 100L)).willReturn(null);

        assertThatThrownBy(() -> addressService.getAddress(1L, 100L))
                .isInstanceOf(AddressNotFoundException.class);
    }

    // --------------------
    // updateAddress
    // --------------------
    @Test
    @DisplayName("주소 수정 성공")
    void updateAddress_success() {
        Long userId = 1L;
        Long addressId = 10L;
        Address address = mock(Address.class);

        given(userRepository.existsById(userId)).willReturn(true);
        given(addressRepository.findByUser_UserIdAndAddressId(userId, addressId)).willReturn(address);

        addressService.updateAddress(userId, addressId,
                new AddressRequest("별칭", "도로명", "12345", "상세"));

        verify(address).updateAddress("별칭", "도로명", "12345", "상세");
    }

    @Test
    @DisplayName("주소 수정 실패 - 주소 없음")
    void updateAddress_fail_addressNotFound() {
        given(userRepository.existsById(1L)).willReturn(true);
        given(addressRepository.findByUser_UserIdAndAddressId(1L, 10L)).willReturn(null);

        assertThatThrownBy(() -> addressService.updateAddress(1L, 10L,
                new AddressRequest("a", "r", "z", "d")))
                .isInstanceOf(AddressNotFoundException.class);
    }

    // --------------------
    // deleteAddress
    // --------------------
    @Test
    @DisplayName("주소 삭제 성공")
    void deleteAddress_success() {
        Long userId = 1L;
        Long addressId = 10L;

        User user = userWithDefault(addressIdOnly(99L)); // 기본주소는 다른 ID
        Address target = addressIdOnly(addressId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(addressRepository.findByUser_UserIdAndAddressId(userId, addressId)).willReturn(target);

        addressService.deleteAddress(userId, addressId);

        verify(addressRepository).deleteByUser_UserIdAndAddressId(userId, addressId);
    }

    @Test
    @DisplayName("주소 삭제 실패 - 기본주소 삭제 불가")
    void deleteAddress_fail_defaultCannotDelete() {
        Long userId = 1L;
        Long addressId = 10L;

        Address defaultAddr = addressIdAndAlias(addressId, "기본집");
        User user = userWithDefault(defaultAddr);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(addressRepository.findByUser_UserIdAndAddressId(userId, addressId)).willReturn(defaultAddr);

        assertThatThrownBy(() -> addressService.deleteAddress(userId, addressId))
                .isInstanceOf(DefaultAddressCannotBeDeletedException.class);

        verify(addressRepository, never()).deleteByUser_UserIdAndAddressId(anyLong(), anyLong());
    }

    // --------------------
    // updateDefaultAddress
    // --------------------
    @Test
    @DisplayName("기본주소 변경 성공")
    void updateDefaultAddress_success() {
        Long userId = 1L;
        Long addressId = 10L;

        User user = mock(User.class);

        Address currentDefault = mock(Address.class);
        when(currentDefault.getAddressId()).thenReturn(1L); // addressId와 다르게

        Address selected = mock(Address.class); // selected는 getAddressId 필요 없음

        when(user.getAddress()).thenReturn(currentDefault);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(addressRepository.findByUser_UserIdAndAddressId(userId, addressId)).willReturn(selected);

        addressService.updateDefaultAddress(userId, addressId);

        verify(user).setDefaultAddress(selected);
    }



    // --------------------
    // getDefaultAddress
    // --------------------
    @Test
    @DisplayName("기본주소 조회 성공")
    void getDefaultAddress_success() {
        Long userId = 1L;
        User user = userWithDefault(addressIdOnly(10L));

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        AddressResponse result = addressService.getDefaultAddress(userId);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("기본주소 조회 실패 - 기본주소 없음")
    void getDefaultAddress_fail_defaultNotFound() {
        Long userId = 1L;
        User user = userWithDefault(null);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> addressService.getDefaultAddress(userId))
                .isInstanceOf(DefaultAddressNotFoundException.class);
    }

    // --------------------
    // helpers (필요한 것만 stubbing)
    // --------------------
    private User userWithDefault(Address defaultAddress) {
        User user = mock(User.class);
        when(user.getAddress()).thenReturn(defaultAddress);
        return user;
    }

    private Address addressIdOnly(Long id) {
        Address a = mock(Address.class);
        when(a.getAddressId()).thenReturn(id);
        return a;
    }

    private Address addressIdAndAlias(Long id, String alias) {
        Address a = addressIdOnly(id);
        when(a.getAlias()).thenReturn(alias);
        return a;
    }
}