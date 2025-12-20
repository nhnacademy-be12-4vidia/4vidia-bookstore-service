package com.nhnacademy._vidiabookstoreservice.user.service.impl;
import com.nhnacademy._vidiabookstoreservice.global.client.CouponClient;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointPolicyRewardRequest;
import com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.FindIdRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.FindPasswordRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.UserSignupRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.event.WelcomeCouponIssueEvent;
import com.nhnacademy._vidiabookstoreservice.user.exception.already.ResignedUserAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.user.exception.already.UserAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.user.exception.notfound.UserNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.repository.GradeRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import com.nhnacademy._vidiabookstoreservice.user.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private GradeRepository gradeRepository;
    @Mock
    private EmailService mailService;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private CouponClient couponClient;
    @Mock
    private PointCommandService pointCommandService;

    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("회원가입 성공")
    void register_success() {
        // given
        // [수정 1] DTO/Record는 Mock 대신 실제 객체를 생성해서 사용합니다.
        UserSignupRequest request = new UserSignupRequest(
                "test@test.com",
                "password",
                "홍길동",
                "010-1234-5678",
                LocalDate.of(1990, 1, 1)
        );

        Grade mockGrade = mock(Grade.class);

        // Stubbing
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(gradeRepository.findByGradeName(GradeName.WELCOME)).willReturn(mockGrade);
        given(bCryptPasswordEncoder.encode(anyString())).willReturn("encodedPw");

        // [수정 2] save() 호출 시 ID가 생성되는 동작을 명시적으로 정의 (doAnswer 사용)
        // 주의: User 엔티티의 ID 필드명이 "userId"가 맞는지 확인하세요. (만약 "id"라면 "id"로 수정)
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "userId", 1L); // ID 강제 주입
            return user;
        }).when(userRepository).save(any(User.class));

        // when
        Long resultId = authService.register(request);

        // then
        assertThat(resultId).isNotNull(); // null이 아님을 먼저 검증
        assertThat(resultId).isEqualTo(1L);

        // 검증
        verify(userRepository).save(any(User.class));
        verify(pointCommandService).rewardByPolicy(any(PointPolicyRewardRequest.class));


        // ✅ 쿠폰은 직접 호출이 아니라 "이벤트 발행"이 맞음
        ArgumentCaptor<WelcomeCouponIssueEvent> captor =
                ArgumentCaptor.forClass(WelcomeCouponIssueEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(1L);

        // ❌ 서비스 단위테스트에서는 couponClient 호출을 기대하면 안 됨 (리스너가 담당)
        verifyNoInteractions(couponClient);

    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 이메일")
    void register_fail_duplicateEmail() {
        // given
        UserSignupRequest mockRequest = mock(UserSignupRequest.class);
        given(mockRequest.email()).willReturn("duplicate@test.com");

        given(userRepository.existsByEmail("duplicate@test.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.register(mockRequest))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("아이디 찾기 성공")
    void findUserId_success() {
        // given
        FindIdRequest mockRequest = mock(FindIdRequest.class);
        given(mockRequest.name()).willReturn("홍길동");
        given(mockRequest.birthday()).willReturn("1990-01-01");
        given(mockRequest.phone()).willReturn("010-1234-5678");

        User mockUser = mock(User.class);
        given(mockUser.getEmail()).willReturn("found@test.com");

        given(userRepository.findByNameAndBirthDateAndPhone(
                eq("홍길동"), any(LocalDate.class), eq("010-1234-5678"))
        ).willReturn(Optional.of(mockUser));

        // when
        String resultEmail = authService.findUserId(mockRequest);

        // then
        assertThat(resultEmail).isEqualTo("found@test.com");
    }

    @Test
    @DisplayName("아이디 찾기 실패 - 사용자 없음")
    void findUserId_fail_notFound() {
        // given
        FindIdRequest mockRequest = mock(FindIdRequest.class);
        given(mockRequest.name()).willReturn("홍길동");
        given(mockRequest.birthday()).willReturn("1990-01-01");
        given(mockRequest.phone()).willReturn("010-0000-0000");

        given(userRepository.findByNameAndBirthDateAndPhone(anyString(), any(LocalDate.class), anyString()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.findUserId(mockRequest))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("비밀번호 초기화 및 메일 발송 성공")
    void restPasswordAndSendMail_success() {
        // given
        FindPasswordRequest mockRequest = mock(FindPasswordRequest.class);
        given(mockRequest.email()).willReturn("test@test.com");
        given(mockRequest.name()).willReturn("홍길동");
        given(mockRequest.phone()).willReturn("010-1234-5678");

        // User 객체 생성 (비밀번호 업데이트 검증을 위해 Mock 대신 실제 객체 사용 권장)
        User user = User.builder()
                .email("test@test.com")
                .name("홍길동")
                .password("oldPassword")
                .build();

        given(userRepository.findByEmailAndNameAndPhone(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(user));
        given(bCryptPasswordEncoder.encode(anyString())).willReturn("newEncodedPassword");

        // when
        String result = authService.restPasswordAndSendMail(mockRequest);

        // then
        assertThat(result).contains("임시 비밀번호가 이메일로 발송되었습니다");
        assertThat(user.getPassword()).isEqualTo("newEncodedPassword");

        verify(mailService).sendTempPassword(eq("test@test.com"), anyString());
    }

    @Test
    @DisplayName("휴면 계정 여부 확인 - 정상 회원 (False)")
    void isDormant_activeUser() {
        // given
        String email = "active@test.com";
        User user = User.builder().email(email).build();
        ReflectionTestUtils.setField(user, "status", UserStatus.ACTIVE);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // when
        Boolean result = authService.isDormant(email);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("휴면 계정 여부 확인 - 휴면 회원 (True)")
    void isDormant_dormantUser() {
        // given
        String email = "dormant@test.com";
        User user = User.builder().email(email).build();
        ReflectionTestUtils.setField(user, "status", UserStatus.DORMANT);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // when
        Boolean result = authService.isDormant(email);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("휴면 계정 여부 확인 - 탈퇴 회원 (Exception)")
    void isDormant_deletedUser() {
        // given
        String email = "deleted@test.com";
        User user = User.builder().email(email).build();
        ReflectionTestUtils.setField(user, "status", UserStatus.DELETED);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> authService.isDormant(email))
                .isInstanceOf(ResignedUserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("휴면 회원 전환 배치 처리 성공")
    void convertDormantUsers_success() {
        // given
        User user1 = User.builder().name("u1").build();
        ReflectionTestUtils.setField(user1, "status", UserStatus.ACTIVE);

        User user2 = User.builder().name("u2").build();
        ReflectionTestUtils.setField(user2, "status", UserStatus.ACTIVE);

        given(userRepository.findActiveUsersNotLoggedInSince(eq(UserStatus.ACTIVE), any(LocalDateTime.class)))
                .willReturn(List.of(user1, user2));

        // when
        int count = authService.convertDormantUsers(LocalDateTime.now());

        // then
        assertThat(count).isEqualTo(2);
        assertThat(user1.getStatus()).isEqualTo(UserStatus.DORMANT);
        assertThat(user2.getStatus()).isEqualTo(UserStatus.DORMANT);
    }

}