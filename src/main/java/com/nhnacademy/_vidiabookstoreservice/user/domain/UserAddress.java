package com.nhnacademy._vidiabookstoreservice.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "user_address")
public class UserAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("주소 식별자")
    private Long userAddressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(name = "alias", length = 20)
    private String alias; //별칭: 집, 회사 등

    @Column(length = 30, name = "address_roadname", nullable = false)
    private String roadAddress; //도로명 주소

    @Column(length = 5, name = "address_postal_number",  nullable = false)
    private String postalAddress; //우편번호 // todo : 변수명 통일해야함

    @Column(length = 30, name = "address_detail")
    private String addressDetail; //상세주소


    public void updateAddress(String alias, String roadAddress, String postalAddress,
                              String addressDetail) {
        this.alias = alias;
        this.roadAddress = roadAddress;
        this.postalAddress = postalAddress;
        this.addressDetail = addressDetail;
    }

}
