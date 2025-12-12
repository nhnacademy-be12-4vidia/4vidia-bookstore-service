package com.nhnacademy._vidiabookstoreservice.user.domain;

import com.nhnacademy._vidiabookstoreservice.global.entity.BaseEntity;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserRole;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Column(name = "password", nullable = false, length = 60)
    private String password;

    @Setter
    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Setter
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Setter
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Setter
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "status", nullable = false)
    private UserStatus status =  UserStatus.ACTIVE; // ACTIVE, DORMANT, DELETED
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.USER; //USER, ADMIN

    @Column(name = "social_id")
    private String socialId; //OAUTH ID
    @Column(name = "provider")
    private String provider; // PAYCO등

    @Column(name = "point")
    private Integer point = 0;

    // 대표주소 (기본주소) FK
    @OneToOne
    @JoinColumn(name = "address_id")
    private Address address;

    // user 등급
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id")
    private Grade grade;


    @Builder
    public User(String email, String password, String name,
                String phone,
                LocalDate birthDate, Grade grade){
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.birthDate = birthDate;
        this.grade = grade;
    }


    /* ===== 연관 관계 ===== */

    // users 1: userAddress N
    //회원이 가진 모든 주소 리스트
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();


    // 비즈니스 로직 메소드
    public void addPoint(int amount){
        this.point += amount;
    }
    public void subtractPoint(int amount){
        this.point -= amount;
    }


    public void setDefaultAddress(Address address) {
        this.address = address;
    }
    public void setStatus(UserStatus userStatus) {
        this.status = userStatus;
    }

//    public void updateUserInfo(String name, String phone, LocalDate birthDate) {
//        if(name !=null && !name.trim().isEmpty()){
//            this.name = name;
//        }
//        if(phone !=null && !phone.trim().isEmpty()){
//            this.phone = phone;
//        }
//        if(birthDate !=null && !birthDate.toString().isEmpty()){
//            this.birthDate = birthDate;
//        }
//    }

    // 휴먼상태인지 확인 (마지막 로그인이 3개월 전이면 )
    public boolean isDormant(){
        // todo : 회원가입하고 로그인을 안하면 -> 몇년이 지나도 휴먼이 안되요? -> 3개월 휴면 처리할때 같이 처리
        if(lastLoginAt == null){
            return false;
        }

        // 마지막 로그인 시간이 현재 시간보다 3개월 이전이면 -> true(휴먼ㅇㅇ)
        return lastLoginAt.isBefore(LocalDateTime.now().minusMonths(3));
    }
    // 비밀번호 변경
    public void updateEncodedPassword(String encodedPassword) {
        // 필요한 경우 검증 로직 추가 가능
        if(password==null || password.isEmpty()){
            throw new IllegalArgumentException("Password cannot be empty");
        }

        this.password = encodedPassword;
    }
}
