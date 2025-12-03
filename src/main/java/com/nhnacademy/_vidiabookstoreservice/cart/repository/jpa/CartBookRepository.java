package com.nhnacademy._vidiabookstoreservice.cart.repository.jpa;

import com.nhnacademy._vidiabookstoreservice.cart.domain.CartBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartBookRepository extends JpaRepository<CartBook, Long> {
}