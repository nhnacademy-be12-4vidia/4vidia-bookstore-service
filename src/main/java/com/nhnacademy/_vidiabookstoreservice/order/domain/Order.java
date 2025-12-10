package com.nhnacademy._vidiabookstoreservice.order.domain;

import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.OrderStatus;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @Column(name = "order_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long orderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    User user;

    @Column(name = "recipient_name", length = 50, nullable = false)
    String recipientName;

    @Column(name = "address_roadname", length = 30, nullable = false)
    String addressRoadname;

    @Column(name = "address_detail", length = 30, nullable = false)
    String addressDetail;

    @Column(name = "zip_code", length = 5, nullable = false)
    String zipCode;

    @Column(name = "recipient_phone", length = 20, nullable = false)
    String recipientPhone;

    @Column(name = "delivery_request", length = 100)
    String deliveryRequest;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "coupon_discount", nullable = false)
    Integer couponDiscount = 0;

    @Column(name = "point_used", nullable = false)
    Integer pointUsed = 0;

    @Column(name = "delivery_date", nullable = false)
    LocalDate deliveryDate;

    @Column(name = "delivery_status", nullable = false)
    DeliveryStatus deliveryStatus = DeliveryStatus.WAITING;

    @Column(name = "actual_delivery_date")
    LocalDate actualDeliveryDate;

    @Column(name = "total_price", nullable = false)
    Integer totalPrice = 0;

    @Column(name = "pay_price", nullable = false)
    Integer payPrice = 0;

    @Column(name = "order_status", nullable = false)
    OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(name = "order_password", length = 30)
    String orderPassword;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    List<OrderItem> orderItems = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = (this.createdAt == null) ? LocalDateTime.now() : this.createdAt;
    }

    @Builder
    public Order(User user, String recipientName, String addressRoadname, String addressDetail, String zipCode,
                 String recipientPhone, String deliveryRequest, int couponDiscount, int pointUsed,
                 LocalDate deliveryDate, int totalPrice, int payPrice) {
        this.user = user;
        this.recipientName = recipientName;
        this.addressRoadname = addressRoadname;
        this.addressDetail = addressDetail;
        this.zipCode = zipCode;
        this.recipientPhone = recipientPhone;
        this.deliveryRequest = deliveryRequest;
        this.couponDiscount = couponDiscount;
        this.pointUsed = pointUsed;
        this.deliveryDate = deliveryDate;
        this.totalPrice = totalPrice;
        this.payPrice = payPrice;
    }
}
