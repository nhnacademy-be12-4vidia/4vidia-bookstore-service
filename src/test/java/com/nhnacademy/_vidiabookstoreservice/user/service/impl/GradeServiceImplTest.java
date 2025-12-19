package com.nhnacademy._vidiabookstoreservice.user.service.impl;

import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.dto.grade.response.GradeResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.UserNetSum;
import com.nhnacademy._vidiabookstoreservice.user.exception.notfound.GradeNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.exception.notfound.UserNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.repository.GradeRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

//@SpringBootTest // 이건 통합테스트..
//@ActiveProfiles("local") // 통합 테스트에서 환경설정파일을 불러올때 사용, 필요없데요
@ExtendWith(MockitoExtension.class) // 단위테스트, Service 계층의 비즈니스 로직 테스트에서 사용
class GradeServiceImplTest {

    @Mock
    private GradeRepository gradeRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GradeServiceImpl gradeService;


    @Mock
    private OrderRepository orderRepository;
    @Mock
    private EntityManager em;

    // 테스트용 User 생성 메서드
    private User createDummyUser(Long userId, Grade grade) {
        User user = User.builder()
                .email("test@test.com")
                .password("1234qwer!")
                .name("테스트유저")
                .birthDate(LocalDate.now())
                .grade(grade)
                .build();

        // userId(pk) 는 오토인크리먼트임 (리플렉션 사용ㅇㅇ)
        ReflectionTestUtils.setField(user, "userId", userId);
        return user;
    }

    // 테스트용 Grade 생성 메서드
    public Grade createDummyGrade(Long gradeId, GradeName gradeName, Integer pointRate) {
        Grade grade = Grade.builder()
                .gradeName(gradeName)
                .pointRate(pointRate)
                .build();

        // gradeId(pk) = auto increasement
        ReflectionTestUtils.setField(grade, "gradeId", gradeId);
        return grade;
    }


    @Test
    @DisplayName("[등급 조회] - 성공")
    void getGrade_success() {
        // given
        Long userId = 1L;

        // GOLD(3) = 포인트적립률: 4%
        Grade grade = createDummyGrade(3L, GradeName.GOLD, 4);
        User user = createDummyUser(userId, grade);

        given(userRepository.findById(userId)).willReturn(Optional.of(user)); // 꼭 Optional 써야하나?

        // when
        GradeResponse response = gradeService.getGrade(userId); // 서비스로직(등급조회) 호출

        // then
        assertThat(response.gradeName()).isEqualTo(grade.getGradeName().name()); // GOLD(2)
        assertThat(response.pointRate()).isEqualTo(grade.getPointRate()); // 4%

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("[등급 조회] - 실패(존재하지 않는 회원)")
    void getGrade_failure_userNotFound() {
        // given
        Long userId = 99L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class,
                () -> gradeService.getGrade(userId) // 서비스로직(등급조회) 호출 시, 예외 던지는지 확인
        );
    }

    @Test
    @DisplayName("[등급 변경] - 성공")
    void updateGrade_success() {
        // given
        Long userId = 1L;
        Long targetGradeId = 2L; // ROYAL(2) 등급

        // 기존 등급: REGULAR(1) = 포인트적립률: 2%
        Grade currentGrade = createDummyGrade(1L, GradeName.REGULAR, 2);
        User user = createDummyUser(userId, currentGrade); // user 생성 시, regular 등급으로 지정

        // 바꿀 등급: ROYAL(2) = 포인트적립률: 3%
        Grade expectedGrade = createDummyGrade(targetGradeId, GradeName.ROYAL, 3);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(gradeRepository.findById(targetGradeId)).willReturn(Optional.of(expectedGrade));

        // when
        gradeService.updateGrade(userId, targetGradeId); // 서비스로직(등급변경) 호출

        // then
        assertThat(user.getGrade()).isEqualTo(expectedGrade);
        assertThat(user.getGrade().getGradeName().name()).isEqualTo(expectedGrade.getGradeName().name());
        assertThat(user.getGrade().getPointRate()).isEqualTo(expectedGrade.getPointRate());
    }

    @Test
    @DisplayName("[등급 변경] - 실패 (변경할 유저 존재하지 않음)")
    void updateGrade_failure_userNotFound() {
        // given
        Long invalidUserId = 99L; // 존재하지 않는 유저 아이디
        Long gradeId = 4L;

        given(userRepository.findById(invalidUserId)).willReturn(Optional.empty()); // 존재하지 않는 유저아이디로 조회 시, empty() 리턴하도록 설정

        // when & then
        assertThrows(UserNotFoundException.class,
                () -> gradeService.updateGrade(invalidUserId, gradeId) // 서비스로직(등급변경) 호출
        );

        verify(gradeRepository, never()).findById(gradeId); // 등급 조회 로직은 호출조차 되지 않았음
    }

    @Test
    @DisplayName("[등급 변경] - 실패 (변경할 등급 존재하지 않음)")
    void updateGrade_failure_gradeNotFound() {
        // given
        Long userId = 1L;
        Long invalidGradeId = 5L; // 존재하지 않는 등급 아이디

        User user = createDummyUser(userId, createDummyGrade(4L, GradeName.PLATINUM, 5));

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(gradeRepository.findById(invalidGradeId)).willReturn(Optional.empty()); // 존재하지 않은 등급으로 조회 시, empty() 리턴하도록 설정

        // when & then
        assertThrows(GradeNotFoundException.class,
                () -> gradeService.updateGrade(userId, invalidGradeId) // 서비스로직(등급변경) 호출
        );
    }

    @Test
    @DisplayName("[월간 등급 산정] - 금액별 등급 변경 및 날짜 계산 검증")
    void recalculateMonthlyGrades_success() {
        // given
        // 1. 모든 등급 정보 Mocking (Service의 loadGradeMap() 통과를 위해 필수)
        // PLATINUM(40만), GOLD(30만), ROYAL(20만), REGULAR(10만), WELCOME(0)
        Grade platinum = createDummyGrade(5L, GradeName.PLATINUM, 5);
        Grade gold = createDummyGrade(4L, GradeName.GOLD, 4);
        Grade royal = createDummyGrade(3L, GradeName.ROYAL, 3);
        Grade regular = createDummyGrade(2L, GradeName.REGULAR, 2);
        Grade welcome = createDummyGrade(1L, GradeName.WELCOME, 1);

        given(gradeRepository.findByGradeName(GradeName.PLATINUM)).willReturn(platinum);
        given(gradeRepository.findByGradeName(GradeName.GOLD)).willReturn(gold);
        given(gradeRepository.findByGradeName(GradeName.ROYAL)).willReturn(royal);
        given(gradeRepository.findByGradeName(GradeName.REGULAR)).willReturn(regular);
        given(gradeRepository.findByGradeName(GradeName.WELCOME)).willReturn(welcome);

        // 2. 테스트용 유저 준비
        // User A: 현재 WELCOME -> 45만원 사용 (PLATINUM 승급 예정)
        User userA = createDummyUser(100L, welcome);
        // User B: 현재 PLATINUM -> 5만원 사용 (WELCOME 강등 예정)
        User userB = createDummyUser(200L, platinum);
        // User C: 아예 주문 내역 없음 (WELCOME 유지)
        User userC = createDummyUser(300L, welcome);

        List<User> userList = List.of(userA, userB, userC);

        // Page mocking: 첫 호출엔 리스트 반환, 두 번째엔 빈 페이지(Loop 종료)
        Page<User> userPage = new PageImpl<>(userList);
        given(userRepository.findAll(any(PageRequest.class)))
                .willReturn(userPage)
                .willReturn(Page.empty());

        // 3. 주문 통계 데이터 Mocking (UserNetSum 인터페이스 구현체 사용)
        List<UserNetSum> netSums = List.of(
                createNetSum(100L, 450_000L), // User A
                createNetSum(200L, 50_000L)   // User B
                // User C는 리스트에 없으므로 0원으로 처리됨
        );

        given(orderRepository.findUserNetSumLast3Months(any(), any(), anyInt()))
                .willReturn(netSums);

        // when
        int updatedCount = gradeService.recalculateMonthlyGrades();

        // then
        // 1. 변경된 유저 수 검증 (UserA, UserB 2명 변경됨. UserC는 그대로라 카운트 X)
        assertThat(updatedCount).isEqualTo(2);

        // 2. 등급 변경 결과 검증
        assertThat(userA.getGrade().getGradeName()).isEqualTo(GradeName.PLATINUM); // 승급 확인
        assertThat(userB.getGrade().getGradeName()).isEqualTo(GradeName.WELCOME);  // 강등 확인
        assertThat(userC.getGrade().getGradeName()).isEqualTo(GradeName.WELCOME);  // 유지 확인

        // 3. 날짜 계산 로직 검증 (ArgumentCaptor 사용)
        // 서비스가 레포지토리에 넘긴 날짜가 진짜 "이번달 1일 ~ 3개월 전 1일"인지 확인
        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(orderRepository).findUserNetSumLast3Months(
                fromCaptor.capture(),
                toCaptor.capture(),
                eq(PointReason.ORDER_CANCEL_REFUND.getCode())
        );

        LocalDateTime capturedFrom = fromCaptor.getValue();
        LocalDateTime capturedTo = toCaptor.getValue();

        // 날짜 검증: from이 to보다 과거여야 하고, 1일이어야 함
        assertThat(capturedFrom).isBefore(capturedTo);
        assertThat(capturedFrom.getDayOfMonth()).isEqualTo(1);
        assertThat(capturedTo.getDayOfMonth()).isEqualTo(1);

        // EntityManager 동작 검증
        verify(em).flush();
        verify(em).clear();
    }

    // --- Helper Methods & Inner Classes ---

    // UserNetSum 인터페이스 가짜 구현체 (테스트용)
    private UserNetSum createNetSum(Long userId, Long netSum) {
        return new UserNetSum() {
            @Override
            public Long getUserId() { return userId; }
            @Override
            public Long getNetSum() { return netSum; }
        };
    }

}

