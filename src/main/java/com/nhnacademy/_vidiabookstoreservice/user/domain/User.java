package com.nhnacademy._vidiabookstoreservice.user.domain;



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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column( nullable = false, length = 50)
    private String email;

    @Column(nullable = false, length = 60)
    private String password;

    @Setter
    @Column(nullable = false, length = 50)
    private String name;

    @Setter
    @Column( nullable = false, length = 50)
    private String phone;

    // Todo  user쪽에서 쿠폰 요청하기 (매월 1일마다)
    @Setter
    private LocalDate birthDate;

    private LocalDateTime lastLoginAt;


    @Column(nullable = false)
    private UserStatus status =  UserStatus.ACTIVE; // ACTIVE, DORMANT, DELETED

    @Column(nullable = false)
    private UserRole role = UserRole.USER; //USER, ADMIN

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Setter
    private LocalDateTime updatedAt;
    private String socialId; //OAUTH ID
    private String provider; // PAYCO등

    private int point = 0;

    // 대표주소 (기본주소) FK
    @OneToOne
    private UserAddress userAddress;

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
    private List<UserAddress> addresses = new ArrayList<>();


    // 비즈니스 로직 메소드

    // 회원 정보 수정
    public void updateProfile(String name, String phone, LocalDate birthDate){
        this.name = name;
        this.phone = phone;
        this.birthDate = birthDate;
    }



    public void setDefaultAddress(UserAddress userAddress) {
        this.userAddress = userAddress;
    }
    public void setStatus(UserStatus userStatus) {
        this.status = userStatus;
    }
    public void updateUserInfo(String name, String phone, LocalDate birthDate) {
        if(name !=null && !name.trim().isEmpty()){
            this.name = name;
        }
        if(phone !=null && !phone.trim().isEmpty()){
            this.phone = phone;
        }
        if(birthDate !=null && !birthDate.toString().isEmpty()){
            this.birthDate = birthDate;
        }
    }
    // 휴먼상태인지 확인 (마지막 로그인이 3개월 전이면 )
    public boolean isDormant(){
        if(lastLoginAt == null){return false;}
        return lastLoginAt.isBefore(LocalDateTime.now().minusMonths(3));
    }
    // 비밀번호 변경
    public void updateEncodedPassword(String encodedPassword) {
        // 필요한 경우 검증 로직 추가 가능
        if(password==null || password.isEmpty()){
            throw new IllegalArgumentException("Password cannot be empty");
        }

        this.password = encodedPassword;
        this.updatedAt = LocalDateTime.now(); // 변경 시간 업데이트
    }
    public void setLastLoginAt(LocalDateTime now) {
        this.lastLoginAt = now;
    }
}
