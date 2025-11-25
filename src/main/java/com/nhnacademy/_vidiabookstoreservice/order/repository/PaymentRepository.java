package com.nhnacademy._vidiabookstoreservice.order.repository;
import com.nhnacademy.order.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
