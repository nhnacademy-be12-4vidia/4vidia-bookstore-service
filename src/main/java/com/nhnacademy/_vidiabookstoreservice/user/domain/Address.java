package com.nhnacademy._vidiabookstoreservice.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("주소 식별자")
    @Column(name = "address_id")
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(name = "alias", length = 20)
    private String alias; //별칭: 집, 회사 등

    @Column(name = "address_roadname", length = 30, nullable = false)
    private String roadAddress; //도로명 주소

    @Column(name = "zip_code", length = 5, nullable = false)
    private String zipCode; //우편번호

    @Column(name = "address_detail", length = 30)
    private String addressDetail; //상세주소


    public void updateAddress(String alias, String roadAddress, String zipCode, String addressDetail) {
        this.alias = alias;
        this.roadAddress = roadAddress;
        this.zipCode = zipCode;
        this.addressDetail = addressDetail;
    }

}
