package com.nhnacademy._vidiabookstoreservice.user.repository;

import com.nhnacademy._vidiabookstoreservice.user.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address,Long> {
    int countByUser_userId(Long userId);

    List<Address> findAllByUser_UserId(Long userId);

    Address findByUser_UserIdAndAddressId(Long userId, Long addressId);

    void deleteByUser_UserIdAndAddressId(Long userId, Long addressId);
}