package com.nhnacademy._vidiabookstoreservice.user.service.impl;

import com.nhnacademy._vidiabookstoreservice.global.client.CouponClient;
import com.nhnacademy._vidiabookstoreservice.point.dto.request.PointPolicyRewardRequest;
import com.nhnacademy._vidiabookstoreservice.point.service.PointCommandService;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserRole;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.FindIdRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.FindPasswordRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.PaycoUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.UserSignupRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.response.OAuth2UserDto;
import com.nhnacademy._vidiabookstoreservice.user.dto.event.BirthdayCouponIssueEvent;
import com.nhnacademy._vidiabookstoreservice.user.dto.event.WelcomeCouponIssueEvent;
import com.nhnacademy._vidiabookstoreservice.user.exception.EmailVerificationExpiredException;
import com.nhnacademy._vidiabookstoreservice.user.exception.already.ResignedUserAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.user.exception.already.UserAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.user.exception.invalid.InvalidAuthCodeException;
import com.nhnacademy._vidiabookstoreservice.user.exception.notfound.UserNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.repository.GradeRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.redis.RedisSignupEmailAuthRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.redis.RedisSignupEmailVerifiedRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private GradeRepository gradeRepository;
    @Mock private EmailService mailService;
    @Mock private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock private CouponClient couponClient;
    @Mock private PointCommandService pointCommandService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private RedisSignupEmailAuthRepository signupEmailAuthRepository;
    @Mock private RedisSignupEmailVerifiedRepository signupEmailVerifiedRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("회원가입 성공 - 일반")
    void register_success() {
        // given
        UserSignupRequest request = new UserSignupRequest(
                "test@test.com",
                "password",
                "홍길동",
                "010-1234-5678",
                LocalDate.of(1990, 1, 1) // 생일 아님
        );

        Grade mockGrade = mock(Grade.class);

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(signupEmailVerifiedRepository.isVerified(anyString())).willReturn(true);
        given(gradeRepository.findByGradeName(GradeName.WELCOME)).willReturn(mockGrade);
        given(bCryptPasswordEncoder.encode(anyString())).willReturn("encodedPw");

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "userId", 1L);
            return user;
        }).when(userRepository).save(any(User.class));

        // when
        Long resultId = authService.register(request);

        // then
        assertThat(resultId).isEqualTo(1L);

        verify(userRepository).save(any(User.class));
        verify(pointCommandService).rewardByPolicy(any(PointPolicyRewardRequest.class));
        verify(signupEmailVerifiedRepository).clear(request.email());

        // Welcome 쿠폰 이벤트 발행 확인
        ArgumentCaptor<WelcomeCouponIssueEvent> captor =
                ArgumentCaptor.forClass(WelcomeCouponIssueEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("회원가입 성공 - 생일인 경우 생일쿠폰 이벤트 발행")
    void register_success_birthday() {
        LocalDate today = LocalDate.now();
        UserSignupRequest request = new UserSignupRequest(
                "birthday@test.com", "pw", "name", "phone",
                today // 오늘 생일
        );

        Grade mockGrade = mock(Grade.class);

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(signupEmailVerifiedRepository.isVerified(anyString())).willReturn(true);
        given(gradeRepository.findByGradeName(GradeName.WELCOME)).willReturn(mockGrade);
        given(bCryptPasswordEncoder.encode(anyString())).willReturn("encodedPw");

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "userId", 1L);
            return user;
        }).when(userRepository).save(any(User.class));

        authService.register(request);

        verify(userRepository).save(any(User.class));
        verify(pointCommandService).rewardByPolicy(any(PointPolicyRewardRequest.class));

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());

        List<Object> capturedEvents = eventCaptor.getAllValues();

        boolean hasWelcomeEvent = capturedEvents.stream()
                .anyMatch(e -> e instanceof WelcomeCouponIssueEvent);
        boolean hasBirthdayEvent = capturedEvents.stream()
                .anyMatch(e -> e instanceof BirthdayCouponIssueEvent);

        assertThat(hasWelcomeEvent).as("WelcomeCouponIssueEvent가 발행되어야 합니다.").isTrue();
        assertThat(hasBirthdayEvent).as("BirthdayCouponIssueEvent가 발행되어야 합니다.").isTrue();
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 인증 미완료")
    void register_fail_unverifiedEmail() {
        UserSignupRequest request = new UserSignupRequest(
                "unverified@test.com", "pw", "name", "phone", LocalDate.now()
        );

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(signupEmailVerifiedRepository.isVerified(anyString())).willReturn(false);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailVerificationExpiredException.class)
                .hasMessageContaining("이메일 인증이 필요합니다.");
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
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("이미 존재하는 회원입니다.");
    }

    @Test
    @DisplayName("아이디 찾기 성공")
    void findUserId_success() {
        FindIdRequest mockRequest = mock(FindIdRequest.class);
        given(mockRequest.name()).willReturn("홍길동");
        given(mockRequest.birthday()).willReturn("1990-01-01");
        given(mockRequest.phone()).willReturn("010-1234-5678");

        User mockUser = mock(User.class);
        given(mockUser.getEmail()).willReturn("found@test.com");

        given(userRepository.findByNameAndBirthDateAndPhone(
                eq("홍길동"), any(LocalDate.class), eq("010-1234-5678"))
        ).willReturn(Optional.of(mockUser));

        String resultEmail = authService.findUserId(mockRequest);
        assertThat(resultEmail).isEqualTo("found@test.com");
    }

    @Test
    @DisplayName("아이디 찾기 실패 - 사용자 없음")
    void findUserId_fail_notFound() {
        FindIdRequest mockRequest = mock(FindIdRequest.class);
        given(mockRequest.name()).willReturn("홍길동");
        given(mockRequest.birthday()).willReturn("1990-01-01");
        given(mockRequest.phone()).willReturn("010-0000-0000");

        given(userRepository.findByNameAndBirthDateAndPhone(anyString(), any(LocalDate.class), anyString()))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.findUserId(mockRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("일치하는 회원정보가 없습니다.");
    }

    @Test
    @DisplayName("비밀번호 초기화 및 메일 발송 성공")
    void restPasswordAndSendMail_success() {
        FindPasswordRequest mockRequest = mock(FindPasswordRequest.class);
        given(mockRequest.email()).willReturn("test@test.com");
        given(mockRequest.name()).willReturn("홍길동");
        given(mockRequest.phone()).willReturn("010-1234-5678");

        User user = User.builder()
                .email("test@test.com")
                .name("홍길동")
                .password("oldPassword")
                .build();

        given(userRepository.findByEmailAndNameAndPhone(anyString(), anyString(), anyString()))
                .willReturn(Optional.of(user));
        given(bCryptPasswordEncoder.encode(anyString())).willReturn("newEncodedPassword");

        String result = authService.restPasswordAndSendMail(mockRequest);

        assertThat(result).contains("임시 비밀번호가 이메일로 발송되었습니다");
        assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
        verify(mailService).sendTempPassword(eq("test@test.com"), anyString());
    }

    @Test
    @DisplayName("이메일 중복 체크 성공")
    void existsByEmail_success() {
        String email = "test@test.com";
        given(userRepository.existsByEmail(email)).willReturn(true);
        assertThat(authService.existsByEmail(email)).isEqualTo(true);
    }

    @Test
    @DisplayName("이메일 중복 체크 실패")
    void existsByEmail_fail() {
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        assertThat(authService.existsByEmail(anyString())).isEqualTo(false);
    }

    @Test
    @DisplayName("휴면 계정 여부 확인 - 정상 회원 (False)")
    void isDormant_activeUser() {
        String email = "active@test.com";
        User user = User.builder().email(email).build();
        ReflectionTestUtils.setField(user, "status", UserStatus.ACTIVE);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        Boolean result = authService.isDormant(email);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("휴면 계정 여부 확인 - 휴면 회원 (True)")
    void isDormant_dormantUser() {
        String email = "dormant@test.com";
        User user = User.builder().email(email).build();
        ReflectionTestUtils.setField(user, "status", UserStatus.DORMANT);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        Boolean result = authService.isDormant(email);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("휴면 계정 여부 확인 - 탈퇴 회원 (Exception)")
    void isDormant_deletedUser() {
        String email = "deleted@test.com";
        User user = User.builder().email(email).build();
        ReflectionTestUtils.setField(user, "status", UserStatus.DELETED);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.isDormant(email))
                .isInstanceOf(ResignedUserAlreadyExistsException.class)
                .hasMessageContaining("이미 탈퇴한 회원입니다");
    }

    @Test
    @DisplayName("휴면 회원 전환 배치 처리 성공")
    void convertDormantUsers_success() {
        User user1 = User.builder().name("u1").build();
        ReflectionTestUtils.setField(user1, "status", UserStatus.ACTIVE);

        User user2 = User.builder().name("u2").build();
        ReflectionTestUtils.setField(user2, "status", UserStatus.ACTIVE);

        given(userRepository.findActiveUsersToDormant(eq(UserStatus.ACTIVE), any(LocalDateTime.class)))
                .willReturn(List.of(user1, user2));

        int count = authService.convertDormantUsers(LocalDateTime.now());

        assertThat(count).isEqualTo(2);
        assertThat(user1.getStatus()).isEqualTo(UserStatus.DORMANT);
        assertThat(user2.getStatus()).isEqualTo(UserStatus.DORMANT);
    }

    @Test
    @DisplayName("OAuth(payco) 인증 회원 찾기 성공 - 기존 회원")
    void findOrCreateOAuthUser_success_find() {
        String provider = "payco";
        Long userId = 1L;

        User user = mock(User.class);
        given(user.getUserId()).willReturn(userId);
        given(user.getEmail()).willReturn("origin@test.com");
        given(user.getRole()).willReturn(UserRole.USER);
        given(user.getStatus()).willReturn(UserStatus.ACTIVE);

        PaycoUserRequest request = new PaycoUserRequest(userId.toString());
        given(userRepository.findByProviderAndSocialId(provider, request.id()))
                .willReturn(Optional.of(user));

        OAuth2UserDto result = authService.findOrCreateOAuthUser(provider, request);

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo("origin@test.com");
    }

    @Test
    @DisplayName("OAuth(payco) 인증 회원 생성 성공 - 새 회원")
    void findOrCreateOAuthUser_success_create() {
        String provider = "payco";
        String paycoId = "123";
        PaycoUserRequest request = new PaycoUserRequest(paycoId);

        given(userRepository.findByProviderAndSocialId(provider, request.id()))
                .willReturn(Optional.empty());

        given(gradeRepository.findByGradeName(GradeName.WELCOME))
                .willReturn(Grade.builder().gradeName(GradeName.WELCOME).build());

        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User savingUser = invocation.getArgument(0);
            ReflectionTestUtils.setField(savingUser, "userId", 1L);
            return savingUser;
        });

        OAuth2UserDto createdUser = authService.findOrCreateOAuthUser(provider, request);

        assertThat(createdUser.getUserId()).isEqualTo(1L);
        assertThat(createdUser.getEmail()).contains("@temp.4vidia.shop");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 인증코드 전송 성공")
    void sendSignupEmailCode_success() {
        String email = "new@test.com";
        given(userRepository.existsByEmail(email)).willReturn(false);

        authService.sendSignupEmailCode(email);

        verify(signupEmailAuthRepository).saveCode(eq(email), anyString());
        verify(mailService).sendSignupAuthCode(eq(email), anyString());
    }

    @Test
    @DisplayName("회원가입 인증코드 전송 실패 - 이미 가입된 회원")
    void sendSignupEmailCode_fail_existsUser() {
        String email = "exists@test.com";
        given(userRepository.existsByEmail(email)).willReturn(true);

        assertThatThrownBy(() -> authService.sendSignupEmailCode(email))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("이미 존재하는 회원입니다.");

        verify(signupEmailAuthRepository, never()).saveCode(anyString(), anyString());
        verify(mailService, never()).sendSignupAuthCode(anyString(), anyString());
    }

    @Test
    @DisplayName("회원가입 인증코드 검증 성공")
    void verifySignupEmailCode_success() {
        String email = "test@test.com";
        String code = "123456";

        given(signupEmailAuthRepository.getCode(email)).willReturn(code);

        authService.verifySignupEmailCode(email, code);

        verify(signupEmailAuthRepository).deleteCode(email);
        verify(signupEmailVerifiedRepository).markVerified(email);
    }

    @Test
    @DisplayName("회원가입 인증코드 검증 실패 - 만료되었거나 코드 없음")
    void verifySignupEmailCode_fail_expired() {
        String email = "test@test.com";
        String code = "123456";

        given(signupEmailAuthRepository.getCode(email)).willReturn(null);

        assertThatThrownBy(() -> authService.verifySignupEmailCode(email, code))
                .isInstanceOf(EmailVerificationExpiredException.class)
                .hasMessageContaining("이메일 인증이 필요합니다.");

        verify(signupEmailVerifiedRepository, never()).markVerified(anyString());
    }

    @Test
    @DisplayName("회원가입 인증코드 검증 실패 - 코드 불일치")
    void verifySignupEmailCode_fail_mismatch() {
        String email = "test@test.com";
        String correctCode = "123456";
        String wrongCode = "000000";

        given(signupEmailAuthRepository.getCode(email)).willReturn(correctCode);

        assertThatThrownBy(() -> authService.verifySignupEmailCode(email, wrongCode))
                .isInstanceOf(InvalidAuthCodeException.class)
                .hasMessageContaining("인증 코드가 일치하지 않습니다.");

        verify(signupEmailVerifiedRepository, never()).markVerified(anyString());
    }
}