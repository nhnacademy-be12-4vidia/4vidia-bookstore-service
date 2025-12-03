package com.nhnacademy._vidiabookstoreservice.cart.repository.jpa;

import com.nhnacademy._vidiabookstoreservice.cart.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
