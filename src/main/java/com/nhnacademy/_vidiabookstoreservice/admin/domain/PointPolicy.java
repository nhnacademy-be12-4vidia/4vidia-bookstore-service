package com.nhnacademy._vidiabookstoreservice.admin.domain;

import com.nhnacademy._vidiabookstoreservice.admin.exception.PointPolicyPriceInvalid;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "point_policy")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_policy_id")
    private Long pointPolicyId;

    @Column(name = "point_name", nullable = false, unique = true, length = 50)
    private String pointName;

    @Column(name = "price")
    private Integer price;

    public void updatePrice(Integer newPrice){
        if(newPrice == null){
            throw new PointPolicyPriceInvalid();
        }
        if(newPrice < 0){
            throw new PointPolicyPriceInvalid();
        }
        this.price = newPrice;
    }
}
