package com.nhnacademy._vidiabookstoreservice.user.repository;

import com.nhnacademy._vidiabookstoreservice.book.config.QueryDslConfig;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 중요: 실제 DB 사용 설정
 @ActiveProfiles("test") // 필요 시 application-dev.yml 설정을 로드하려면 주석 해제
@org.springframework.transaction.annotation.Transactional
@Import(QueryDslConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Grade grade;
    private User user;

    @BeforeEach
    void setUp() {
        // Grade 저장 (User 생성 시 필요)
        grade = Grade.builder()
                .gradeName(GradeName.WELCOME)
                .pointRate(1)
                .build();
        entityManager.persist(grade);

        // 기본 User 생성
        user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .name("홍길동")
                .phone("010-1234-5678")
                .birthDate(LocalDate.of(1990, 1, 1))
                .grade(grade)
                .build();
        user.setStatus(UserStatus.ACTIVE);
        entityManager.persist(user);
    }

    // ... 나머지 테스트 메서드들은 이전과 동일 ...

    @Test
    @DisplayName("이메일 존재 여부 확인")
    void existsByEmail() {
        boolean exists = userRepository.existsByEmail("test@example.com");
        assertThat(exists).isTrue();

        boolean notExists = userRepository.existsByEmail("unkonw@example.com");
        assertThat(notExists).isFalse();
    }


    @Test
    @DisplayName("아이디 찾기 - 이름, 생일, 전화번호로 조회")
    void findByNameAndBirthDateAndPhone(){
        Optional<User> foundUser = userRepository.findByNameAndBirthDateAndPhone(
                "홍길동",
                LocalDate.of(1990,1,1),
                "010-1234-5678"
        );
        assertThat(foundUser.isPresent()).isTrue();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("비밀번호 찾기 - 이메일, 이름, 전화번호로 조회")
    void findByEmailAndNameAndPhone(){
        Optional<User> foundUser = userRepository.findByEmailAndNameAndPhone(
                "test@example.com",
                "홍길동",
                "010-1234-5678"
        );
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUserId()).isEqualTo(user.getUserId());

    }

    @Test
    @DisplayName("User ID로 사용자 조회")
    void findByUserId(){
        Optional<User> foundUser = userRepository.findByUserId(user.getUserId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("3개월 이전 로그인하지 않은 Active 사용자 조회")
    void findActiveUsersNotLoggedInSince(){
        // 4개월 전 로그인 기록이 있는 사용자 추가
        User oldLoginUser = User.builder()
                .email("old@example.com")
                .password("pw")
                .name("OldUser")
                .phone("010-1234-5678")
                .birthDate(LocalDate.of(1980,1,1))
                .grade(grade)
                .build();

        oldLoginUser.setStatus(UserStatus.ACTIVE);
        oldLoginUser.setLastLoginAt(LocalDateTime.now().minusMonths(4));
        entityManager.persist(oldLoginUser);

        // 최근 로그인 사용자 (조회되지 않아야 함)
        user.setLastLoginAt(LocalDateTime.now().minusMonths(1));
        entityManager.persist(user);

        // 3개월 기준 설정
        LocalDateTime threshold = LocalDateTime.now().minusMonths(3);

        // 조회 실행
        List<User> dormantCandidates = userRepository.findActiveUsersToDormant(UserStatus.ACTIVE, threshold);

        assertThat(dormantCandidates)
                .extracting(User::getEmail)
                .contains("old@example.com")
                .doesNotContain("recent@example.com");


    }

    @Test
    @DisplayName("Active 사용자 중 로그인 기록이 없는 사용자도 휴면 대상 조회 포함 확인")
    void findActiveUsersNotLoggedInSince_NullLastLogin() {
        // 로그인 기록이 없는 사용자 (lastLoginAt == null )
        User neverLoggedInUser = User.builder()
                .email("naver@example.com")
                .password("pw")
                .name("testName")
                .phone("010-1234-5534")
                .birthDate(LocalDate.of(2000, 1, 1))
                .grade(grade)
                .build();

        neverLoggedInUser.setStatus(UserStatus.ACTIVE);
        entityManager.persist(neverLoggedInUser);

        LocalDateTime threshold = LocalDateTime.now().minusMonths(3);

        List<User> result = userRepository.findActiveUsersToDormant(UserStatus.ACTIVE, threshold);

        // 현재 user (마지막 로그인 null)와 neverLoggedInUser 둘다 조회되어야 함(setup의 user도 로그인 기록 없음)
        assertThat(result).extracting("email")
                .contains("test@example.com", "naver@example.com");
        
    }

    @Test
    @DisplayName("관리자용 회원 검색 - 이메일 검색")
    void searchAdminUsers_ByEmail(){
        Pageable pageable = PageRequest.of(0, 10);
        // 키워드 "test"로 검색
        Page<User> result = userRepository.searchAdminUsers(null, "test",pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("관리자용 회원 검색 - 이름 검색 및 상태 필터링")
    void searchAdminUsers_ByNameAndStatus(){
        Pageable pageable = PageRequest.of(0, 10);

        // 상태가 ACTIVE이고 이름에 "길동"이 들어가는 사용자 검색
        Page<User> result = userRepository.searchAdminUsers(UserStatus.ACTIVE,"홍길동", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("홍길동");
    }











}