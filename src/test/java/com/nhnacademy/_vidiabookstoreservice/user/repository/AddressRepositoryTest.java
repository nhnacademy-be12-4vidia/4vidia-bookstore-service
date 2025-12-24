package com.nhnacademy._vidiabookstoreservice.user.repository;

import com.nhnacademy._vidiabookstoreservice.book.config.QueryDslConfig;
import com.nhnacademy._vidiabookstoreservice.user.domain.Address;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class AddressRepositoryTest {
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private TestEntityManager testEntityManager;

    private User user;

    @BeforeEach
    void setUp(){
        // 1. Grade 생성 및 저장
        Grade grade = Grade.builder()
                .gradeName(GradeName.WELCOME)
                .pointRate(1)
                .build();
        testEntityManager.persist(grade);

        // 2. User 생성 및 저장
        user = User.builder()
                .email("address_test@example.com")
                .password("encodedPassword")
                .name("배송지테스터")
                .phone("010-1234-5678")
                .birthDate(LocalDate.of(1995,5,5))
                .grade(grade)
                .build();

        user.setStatus(UserStatus.ACTIVE);
        testEntityManager.persist(user);

    }

    @Test
    @DisplayName("배송지 저장 및 조회")
    void saveAndFindAddress(){
        //given
        Address address = Address.builder()
                .user(user)
                .alias("우리집")
                .zipCode("12345")
                .roadAddress("광주광역시 동구 필문대로")
                .addressDetail("조선대학교 it공과대학")
                .build();

        // when
        Address savedAddress = addressRepository.save(address);
        testEntityManager.flush();
        testEntityManager.clear(); // 1차 캐시 비우기

        // then
        Optional<Address> foundAddress = addressRepository.findById(savedAddress.getAddressId());

        assertThat(foundAddress).isPresent();
        assertThat(foundAddress.get().getAlias()).isEqualTo("우리집");
        assertThat(foundAddress.get().getUser().getUserId()).isEqualTo(user.getUserId());
    }

    @Test
    @DisplayName("특정 회원의 배송지 목록 조회")
    void findByUser_UserId(){
        // given
        Address addr1 = Address.builder().user(user).alias("집").zipCode("11111").roadAddress("주소1").addressDetail("상세1").build();
        Address addr2 = Address.builder().user(user).alias("회사").zipCode("22222").roadAddress("주소2").addressDetail("상세2").build();

        testEntityManager.persist(addr1);
        testEntityManager.persist(addr2);
        testEntityManager.flush();
        testEntityManager.clear();

        //when
        // AddressRepository에 findByUser_UserId 또는 findByUserId 메서드가 있다고 가정
        List<Address> addresses = addressRepository.findAllByUser_UserId(user.getUserId());

        //then
        assertThat(addresses).hasSize(2);
        assertThat(addresses).extracting("alias")
                .containsExactlyInAnyOrder("집","회사");
    }

    @Test
    @DisplayName("특정 회원의 배송지 개수 조회")
    void countByUser_UserId(){
        //given
        Address addr1 = Address.builder().user(user).alias("집")
                .zipCode("11111").roadAddress("주소1").addressDetail("상세1").build();
        testEntityManager.persist(addr1);

        //when
        long count = addressRepository.countByUser_userId(user.getUserId());

        //then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("배송지 삭제")
    void deleteAddress(){
        //given
        Address address = Address.builder()
                .user(user)
                .alias("삭제할 주소")
                .zipCode("00000")
                .roadAddress("도로명")
                .addressDetail("상세")
                .build();

        Address saved = addressRepository.save(address);
        Long addressId = saved.getAddressId();

        //when
        addressRepository.deleteById(addressId);
        testEntityManager.flush();
        testEntityManager.clear();

        //then
        boolean exists = addressRepository.existsById(addressId);
        assertThat(exists).isFalse();
    }





}