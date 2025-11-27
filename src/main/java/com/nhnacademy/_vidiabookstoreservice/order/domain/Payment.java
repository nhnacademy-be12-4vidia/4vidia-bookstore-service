package com.nhnacademy._vidiabookstoreservice.order.domain;

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

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

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
    public Payment(Order order, String payStatus, String payMethod, long amount, String paymentKey, String sendOrderId) {
        this.order = order;
        this.payStatus = payStatus;
        this.payMethod = payMethod;
        this.amount = (int) amount;
        this.paymentKey = paymentKey;
        this.sendOrderId = sendOrderId;
    }
}
