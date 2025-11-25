package com.nhnacademy._vidiabookstoreservice.order.domain;


import com.nhnacademy.order.domain.dto.PaymentCreateRequest;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @Column(name = "payment_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long paymentId;

    @Column(name = "order_id", nullable = false)
    Long orderId;

    @Column(name = "pay_status", nullable = false)
    String payStatus;

    @Column(name = "pay_method", nullable = false)
    String payMethod;

    @Column(name = "amount", nullable = false)
    Integer amount;

    @Column(name = "payment_key", nullable = false)
    String paymentKey;

    @Column(name = "send_order_id", nullable = false)
    String sendOrderId;

    @Builder
    public Payment(PaymentCreateRequest paymentCreateRequest) {
        this.orderId = paymentCreateRequest.orderId();
        this.payStatus = paymentCreateRequest.payStatus();
        this.payMethod = paymentCreateRequest.payMethod();
        this.amount = paymentCreateRequest.amount().intValue();
        this.paymentKey = paymentCreateRequest.paymentKey();
        this.sendOrderId = paymentCreateRequest.sendOrderId();
    }
}
