package com.nhnacademy._vidiabookstoreservice.user.repository;

import com.nhnacademy._vidiabookstoreservice.user.domain.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<UserAddress,Long> {
    int countByUser_userId(Long userId);

    List<UserAddress> findAllByUser_UserId(Long userId);

    UserAddress findByUser_UserIdAndUserAddressId(Long userUserId, Long userAddressId);

    void deleteByUser_UserIdAndUserAddressId(Long userUserId, Long userAddressId);
}