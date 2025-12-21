package com.nhnacademy._vidiabookstoreservice.user.service.impl;

import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.exception.AuthCodeExpiredException;
import com.nhnacademy._vidiabookstoreservice.user.exception.invalid.InvalidAuthCodeException;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.redis.RedisDormantAutoRepository;
import com.nhnacademy._vidiabookstoreservice.user.sender.DoorayMessageSender;
import com.nhnacademy._vidiabookstoreservice.user.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DormantAuthServiceImplTest {

    @Mock
    private RedisDormantAutoRepository autoRepository;

    @Mock
    private DoorayMessageSender webhookSender;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DormantAuthServiceImpl dormantAuthService;

    @Test
    @DisplayName("휴면 인증 코드 발송 성공 (두레이)")
    void sendAuthCode_success() {
        // given
        String email = "test@test.com";
        String webhookUrl = "https://hook.dooray.com/services/...";

        // when
        dormantAuthService.sendAuthCode(email, webhookUrl);

        // then
        // 1. Redis에 저장된 코드를 캡처 (랜덤 값이라 예측 불가하므로)
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(autoRepository).saveCode(eq(email), codeCaptor.capture());

        String capturedCode = codeCaptor.getValue();

        // 2. 캡처한 코드가 6자리 숫자인지 확인
        assertThat(capturedCode).hasSize(6).containsOnlyDigits();

        // 3. 두레이 메시지 전송 시, 저장된 그 코드가 포함되어 있는지 검증
        verify(webhookSender).send(eq(webhookUrl), anyString(), argThat(body -> body.contains(capturedCode)));
    }

    @Test
    @DisplayName("휴면 인증 코드 발송 성공 (이메일)")
    void sendAuthCodeByEmail_success() {
        // given
        String loginEmail = " LoginUser@test.com "; // 공백 및 대소문자 섞임
        String sendToEmail = "target@test.com";
        String normalizedEmail = "loginuser@test.com"; // 예상되는 정규화된 이메일

        // when
        dormantAuthService.sendAuthCodeByEmail(loginEmail, sendToEmail);

        // then
        // 1. Redis 저장 검증 (로그인 이메일이 소문자+trim 되었는지, 코드가 캡처되는지)
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(autoRepository).saveCode(eq(normalizedEmail), codeCaptor.capture());

        String generatedCode = codeCaptor.getValue();

        // 2. 이메일 발송 검증 (생성된 코드가 올바르게 전달되었는지)
        verify(emailService).sendDormantAuthCode(eq(sendToEmail), eq(generatedCode));
    }

    @Test
    @DisplayName("인증 코드 검증 성공 - 상태 ACTIVE 변경")
    void verifyAuthCode_success() {
        // given
        String email = "dormant@test.com";
        String code = "123456";

        // Redis에서 올바른 코드가 조회된다고 가정
        given(autoRepository.getCode(email)).willReturn(code);

        // 실제 User 객체 사용 (Mock 대신)
        User user = User.builder()
                .email(email)
                .name("휴면유저")
                .build();
        // 초기 상태 설정 (setter가 없으면 Reflection 사용, 있으면 setter 사용)
        user.setStatus(UserStatus.DORMANT);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // when
        dormantAuthService.verifyAuthCode(email, code);

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE); // 상태 변경 확인
        verify(autoRepository).deleteCode(email); // 코드 삭제 호출 확인
    }

    @Test
    @DisplayName("인증 코드 검증 실패 - 만료된 코드 (Redis null)")
    void verifyAuthCode_fail_expired() {
        // given
        String email = "test@test.com";
        String code = "123456";

        // Redis에 코드가 없음 (만료됨)
        given(autoRepository.getCode(email)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> dormantAuthService.verifyAuthCode(email, code))
                .isInstanceOf(AuthCodeExpiredException.class)
                .hasMessage("인증 코드가 만료되었습니다.");
    }

    @Test
    @DisplayName("인증 코드 검증 실패 - 불일치")
    void verifyAuthCode_fail_invalid() {
        // given
        String email = "test@test.com";
        String inputCode = "111111";
        String savedCode = "999999";

        // Redis에는 다른 코드가 저장되어 있음
        given(autoRepository.getCode(email)).willReturn(savedCode);

        // when & then
        assertThatThrownBy(() -> dormantAuthService.verifyAuthCode(email, inputCode))
                .isInstanceOf(InvalidAuthCodeException.class)
                .hasMessage("인증 코드가 일치하지 않습니다.");
    }

    // argThat 사용을 위한 헬퍼 메서드 대신 람다식 사용함 (argThat(body -> ...))
}