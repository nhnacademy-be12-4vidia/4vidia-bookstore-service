package com.nhnacademy._vidiabookstoreservice.order.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "packaging_option")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PackagingOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "packaging_option_id", nullable = false)
    Long packagingOptionId;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "price", nullable = false)
    Integer price;

    @Builder
    public PackagingOption(String name, Integer price) {
        this.name = name;
        this.price = price;
    }
}
