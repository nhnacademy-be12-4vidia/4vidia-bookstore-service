package com.nhnacademy._vidiabookstoreservice.order.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "packaging")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Packaging {

    @Id
    @Column(name = "packaging_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long packagingId;

    @ManyToOne
    @JoinColumn(name = "order_item_id", nullable = false)
    OrderItem orderItem;

    @ManyToOne
    @JoinColumn(name = "packaging_option_id", nullable = false)
    PackagingOption packagingOption;

    @Builder
    public Packaging(OrderItem orderItem, PackagingOption packagingOption) {
        this.orderItem = orderItem;
        this.packagingOption = packagingOption;
    }

}
