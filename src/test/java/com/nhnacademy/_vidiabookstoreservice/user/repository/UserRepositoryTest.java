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
    void findActiveUsersNotLoggedInSince() {
        // Given: 4개월 전 로그인 기록이 있는 사용자 (조회 대상)
        User oldLoginUser = User.builder()
                .email("old@example.com")
                .password("pw")
                .name("OldUser")
                .phone("010-1234-5671")
                .birthDate(LocalDate.of(1980, 1, 1))
                .grade(grade)
                .build();
        oldLoginUser.setStatus(UserStatus.ACTIVE);
        oldLoginUser.setLastLoginAt(LocalDateTime.now().minusMonths(4));
        entityManager.persist(oldLoginUser);

        // Given: 최근 로그인 사용자 (조회되지 않아야 함)
        User recentLoginUser = User.builder()
                .email("recent@example.com")
                .password("pw")
                .name("RecentUser")
                .phone("010-1234-5672")
                .birthDate(LocalDate.of(1995, 1, 1))
                .grade(grade)
                .build();
        recentLoginUser.setStatus(UserStatus.ACTIVE);
        recentLoginUser.setLastLoginAt(LocalDateTime.now().minusMonths(1));
        entityManager.persist(recentLoginUser);

        entityManager.flush();
        entityManager.clear();

        // 3개월 기준 설정
        LocalDateTime threshold = LocalDateTime.now().minusMonths(3);

        // When
        List<User> dormantCandidates = userRepository.findActiveUsersToDormant(UserStatus.ACTIVE, threshold);

        // Then
        assertThat(dormantCandidates)
                .extracting(User::getEmail)
                .contains("old@example.com")
                .doesNotContain("recent@example.com");
    }

    @Test
    @DisplayName("Active 사용자 중 로그인 기록이 없는 사용자도 가입일 기준 휴면 대상 포함 확인")
    void findActiveUsersNotLoggedInSince_NullLastLogin() {
        // Given
        User neverLoggedInUser = User.builder()
                .email("never@example.com")
                .password("pw")
                .name("testName")
                .phone("010-1234-5534")
                .birthDate(LocalDate.of(2000, 1, 1))
                .grade(grade)
                .build();
        neverLoggedInUser.setStatus(UserStatus.ACTIVE);
        entityManager.persist(neverLoggedInUser);
        entityManager.flush();

        // DB의 created_at이 'date' 타입이므로, 'LocalDateTime'의 'now().plusSeconds(1)'을 넘기면 에러가 납니다.
        // 쿼리에서 요구하는 논리적 시점을 맞추기 위해 내일 날짜의 자정으로 설정합니다.
        LocalDateTime threshold = LocalDate.now().plusDays(1).atStartOfDay();

        // When
        List<User> result = userRepository.findActiveUsersToDormant(UserStatus.ACTIVE, threshold);

        // Then
        assertThat(result).extracting("email")
                .contains("test@example.com", "never@example.com");
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