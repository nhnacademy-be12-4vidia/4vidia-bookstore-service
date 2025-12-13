package com.nhnacademy._vidiabookstoreservice.user.service.impl;

import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.dto.grade.response.GradeResponse;
import com.nhnacademy._vidiabookstoreservice.user.exception.GradeNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.exception.UserNotFoundByUserIdException;
import com.nhnacademy._vidiabookstoreservice.user.repository.GradeRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

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
        assertThrows(UserNotFoundByUserIdException.class,
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
        assertThrows(UserNotFoundByUserIdException.class,
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

}

