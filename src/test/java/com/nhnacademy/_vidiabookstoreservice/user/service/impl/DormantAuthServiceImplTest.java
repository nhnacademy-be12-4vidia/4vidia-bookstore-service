package com.nhnacademy._vidiabookstoreservice.user.service.impl;

import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.exception.AuthCodeExpiredException;
import com.nhnacademy._vidiabookstoreservice.user.exception.invalid.InvalidAuthCodeException;
import com.nhnacademy._vidiabookstoreservice.user.exception.notfound.UserNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.redis.RedisDormantAutoRepository;
import com.nhnacademy._vidiabookstoreservice.user.sender.DoorayMessageSender;
import com.nhnacademy._vidiabookstoreservice.user.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
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
    @DisplayName("sendAuthCode: 이메일 정규화 후 Redis 저장 + Dooray 전송 호출")
    void sendAuthCode_success_savesRedisAndSendsDooray() {
        // given
        String rawEmail = " Test@Email.com ";
        String normalizedEmail = "test@email.com";
        String webhookUrl = "https://hook.dooray.com/services/xxx";

        // when
        dormantAuthService.sendAuthCode(rawEmail, webhookUrl);

        // then
        // code는 랜덤이라 값 비교 대신: 호출된 email이 정규화 되었는지 + code가 null/blank 아닌지
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        verify(autoRepository, times(1)).saveCode(emailCaptor.capture(), codeCaptor.capture());
        assertThat(emailCaptor.getValue()).isEqualTo(normalizedEmail);
        assertThat(codeCaptor.getValue()).matches("\\d{6}");

        // Dooray send도 호출 여부만 검증 (title/body는 대략 포함 여부 체크)
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        verify(webhookSender, times(1)).send(urlCaptor.capture(), titleCaptor.capture(), bodyCaptor.capture());

        assertThat(urlCaptor.getValue()).isEqualTo(webhookUrl);
        assertThat(titleCaptor.getValue()).contains("휴면 계정 인증코드");
        assertThat(bodyCaptor.getValue()).contains("인증코드");
    }

    @Test
    @DisplayName("verifyAuthCode: Redis에 코드가 없으면 AuthCodeExpiredException")
    void verifyAuthCode_expired_throwsAuthCodeExpiredException() {
        // given
        String email = "test@email.com";
        when(autoRepository.getCode(email)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> dormantAuthService.verifyAuthCode(email, "123456"))
                .isInstanceOf(AuthCodeExpiredException.class);

        verify(userRepository, never()).findByEmail(anyString());
        verify(autoRepository, never()).deleteCode(anyString());
    }

    @Test
    @DisplayName("verifyAuthCode: 코드 불일치면 InvalidAuthCodeException")
    void verifyAuthCode_mismatch_throwsInvalidAuthCodeException() {
        // given
        String email = "test@email.com";
        when(autoRepository.getCode(email)).thenReturn("111111");

        // when & then
        assertThatThrownBy(() -> dormantAuthService.verifyAuthCode(email, "222222"))
                .isInstanceOf(InvalidAuthCodeException.class);

        verify(userRepository, never()).findByEmail(anyString());
        verify(autoRepository, never()).deleteCode(anyString());
    }

    @Test
    @DisplayName("verifyAuthCode: 유저가 없으면 UserNotFoundException")
    void verifyAuthCode_userNotFound_throwsUserNotFoundException() {
        // given
        String email = "test@email.com";
        when(autoRepository.getCode(email)).thenReturn("123456");
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dormantAuthService.verifyAuthCode(email, "123456"))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, never()).save(any());
        verify(autoRepository, never()).deleteCode(anyString());
    }

    @Test
    @DisplayName("verifyAuthCode: 성공 시 유저 상태 ACTIVE로 변경하고 저장 + Redis 코드 삭제")
    void verifyAuthCode_success_updatesUserAndDeletesRedisCode() {
        // given
        String email = "test@email.com";
        String code = "123456";

        when(autoRepository.getCode(email)).thenReturn(code);

        User user = mock(User.class); // 엔티티 실제 생성이 번거로우면 mock으로 충분
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // when
        dormantAuthService.verifyAuthCode(email, code);

        // then
        verify(user, times(1)).setStatus(UserStatus.ACTIVE);
        verify(userRepository, times(1)).save(user);
        verify(autoRepository, times(1)).deleteCode(email);
    }

    @Test
    @DisplayName("sendAuthCodeByEmail: 성공 시 Redis 저장 후 이메일 발송")
    void sendAuthCodeByEmail_success_savesRedisAndSendsEmail() {
        // given
        String loginEmail = "  Login@Email.com ";
        String normalizedLoginEmail = "login@email.com";
        String sendToEmail = "target@email.com";

        // when
        dormantAuthService.sendAuthCodeByEmail(loginEmail, sendToEmail);

        // then
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        verify(autoRepository, times(1)).saveCode(emailCaptor.capture(), codeCaptor.capture());
        assertThat(emailCaptor.getValue()).isEqualTo(normalizedLoginEmail);
        assertThat(codeCaptor.getValue()).matches("\\d{6}");

        verify(emailService, times(1)).sendDormantAuthCode(eq(sendToEmail), anyString());
        verify(autoRepository, never()).deleteCode(anyString());
    }

    @Test
    @DisplayName("sendAuthCodeByEmail: 이메일 전송 실패 시 Redis에 저장된 코드 삭제 후 예외 재던짐")
    void sendAuthCodeByEmail_mailFail_deletesRedisAndRethrows() {
        // given
        String loginEmail = "  Login@Email.com ";
        String normalizedLoginEmail = "login@email.com";
        String sendToEmail = "target@email.com";

        doThrow(new RuntimeException("mail fail"))
                .when(emailService).sendDormantAuthCode(eq(sendToEmail), anyString());

        // when & then
        assertThatThrownBy(() -> dormantAuthService.sendAuthCodeByEmail(loginEmail, sendToEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("mail fail");

        verify(autoRepository, times(1)).saveCode(eq(normalizedLoginEmail), anyString());
        verify(autoRepository, times(1)).deleteCode(eq(normalizedLoginEmail));
    }
}
