package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserRole;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.*;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.response.OAuth2UserDto;
import com.nhnacademy._vidiabookstoreservice.user.dto.dormant.request.DormantSendCodeByEmailRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.dormant.request.DormantSendCodeRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.dormant.request.DormantVerifyRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.UpdateLastLoginRequest;
import com.nhnacademy._vidiabookstoreservice.user.repository.GradeRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import com.nhnacademy._vidiabookstoreservice.user.service.AuthService;
import com.nhnacademy._vidiabookstoreservice.user.service.DormantAuthService;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import org.h2.engine.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends SupportControllerTest {

    @MockitoBean private AuthService authService;
    @MockitoBean private UserService userService;
    @MockitoBean private DormantAuthService dormantAuthService;

    private final String TEST_EMAIL = "auth_test@test.com";
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void initData() { }

    @Test
    @DisplayName("[회원가입]")
    void signup() throws Exception {
        UserSignupRequest request = new UserSignupRequest(
                "signup_new@test.com",
                "1234qwer!",
                "홍길동",
                "01012341234",
                LocalDate.now().minusYears(20)
        );

        given(authService.register(any(UserSignupRequest.class))).willReturn(1L);

        this.mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(document("auth-signup-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("password").description("비밀번호"),
                                fieldWithPath("name").description("이름"),
                                fieldWithPath("phone").description("전화번호"),
                                fieldWithPath("birthDate").description("생년월일")
                        )
                ));
    }

    @Test
    @DisplayName("[페이코 계정 찾기/만들기]")
    void findOrCreateByPaycoId() throws Exception {
        Map<String, Object> request = Map.of(
                "id",
                "payco_12345"
        );

        User mockUser = User.builder()
                .email("payco@test.com")
                .build();
        mockUser.setStatus(UserStatus.ACTIVE);

        OAuth2UserDto mockResponse = OAuth2UserDto.fromEntity(mockUser);
        given(authService.findOrCreateOAuthUser(anyString(), any())).willReturn(mockResponse);

        mockMvc.perform(post("/auth/payco/find-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("auth-payco-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("id").description("페이코 식별 ID")
                        ),
                        responseFields(
                                fieldWithPath("userId").description("회원 식별 ID"),
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("role").description("권한"),
                                fieldWithPath("status").description("회원 상태")
                        )
                ));
    }

    @Test
    @DisplayName("[아이디(이메일) 찾기]")
    void findUserId() throws Exception {
        FindIdRequest request = new FindIdRequest(
                "인증테스트유저",
                LocalDate.now().minusYears(20).toString(),
                "01011112222"
        );

        given(authService.findUserId(any(FindIdRequest.class))).willReturn(TEST_EMAIL);

        mockMvc.perform(post("/auth/find-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(TEST_EMAIL))
                .andDo(document("auth-find-id-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("name").description("이름"),
                                fieldWithPath("birthday").description("생년월일"),
                                fieldWithPath("phone").description("전화번호")
                        )
                ));
    }

    @Test
    @DisplayName("[비밀번호 초기화]")
    void findPassword() throws Exception {
        FindPasswordRequest request = new FindPasswordRequest(
                TEST_EMAIL,
                "인증테스트유저",
                "01011112222"
        );

        given(authService.restPasswordAndSendMail(any(FindPasswordRequest.class))).willReturn("tempPassword123");

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("auth-reset-password-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("name").description("이름"),
                                fieldWithPath("phone").description("전화번호")
                        )
                ));
    }

    @Test
    @DisplayName("[이메일 중복 확인]")
    void checkEmail() throws Exception {
        given(authService.existsByEmail(anyString())).willReturn(true);

        mockMvc.perform(get("/auth/emails/exists")
                        .param("email", TEST_EMAIL))
                .andExpect(status().isOk())
                .andExpect(content().string("true"))
                .andDo(document("auth-check-email-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        queryParameters(
                                parameterWithName("email").description("중복 확인할 이메일")
                        )
                ));
    }

    @Test
    @DisplayName("[휴면 여부 확인]")
    void checkDormant() throws Exception {
        mockMvc.perform(get("/auth/dormant")
                        .param("email", TEST_EMAIL))
                .andExpect(status().isOk())
                .andExpect(content().string("false")) // 활성 유저이므로 false
                .andDo(document("auth-check-dormant-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("email").description("확인할 이메일")
                        )
                ));
    }

    @Test
    @DisplayName("[휴면 인증코드 전송]")
    void send() throws Exception {
        DormantSendCodeRequest request = new DormantSendCodeRequest(
                TEST_EMAIL,
                "https://webhook.url"
        );

        willDoNothing().given(dormantAuthService).sendAuthCode(anyString(), anyString());

        mockMvc.perform(post("/auth/dormant/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("auth-dormant-send-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("webhookUrl").description("두레이 웹훅 URL")
                        )
                ));
    }

    @Test
    @DisplayName("[휴면 인증코드 검증]")
    void verify() throws Exception {
        // Note: 실제 검증 로직 통과를 위해선 Redis 등에 코드가 저장되어 있어야 할 수 있습니다.
        // 테스트 환경에 따라 Mocking이 필요할 수 있음.
        DormantVerifyRequest request = new DormantVerifyRequest(
                TEST_EMAIL,
                "123456"
        );

        willDoNothing().given(dormantAuthService).verifyAuthCode(anyString(), anyString());

        mockMvc.perform(post("/auth/dormant/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("auth-dormant-verify-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("code").description("인증코드")
                        )
                ));
    }

    @Test
    @DisplayName("[휴면 인증코드 이메일 전송]")
    void sendCodeByEmail() throws Exception {
        DormantSendCodeByEmailRequest request = new DormantSendCodeByEmailRequest(
                TEST_EMAIL,
                "contact@test.com"
        );

        willDoNothing().given(dormantAuthService).sendAuthCodeByEmail(anyString(), anyString());

        mockMvc.perform(post("/auth/dormant/send-code/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("auth-dormant-email-send-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").description("휴면 계정 이메일"),
                                fieldWithPath("contactEmail").description("인증 코드를 받을 연락처 이메일")
                        )
                ));
    }

    @Test
    @DisplayName("[마지막 로그인 시간 업데이트]")
    void updateLastLoginAt() throws Exception {
        UpdateLastLoginRequest request = new UpdateLastLoginRequest(TEST_EMAIL);

        willDoNothing().given(userService).updateLastLoginAt(anyString());

        mockMvc.perform(put("/auth/last-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("auth-last-login-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").description("로그인한 이메일")
                        )
                ));
    }

    @Test
    @DisplayName("[회원가입 이메일 인증코드 전송]")
    void sendSignupEmailCode() throws Exception {
        EmailSendCodeRequest request = new EmailSendCodeRequest(TEST_EMAIL);

        willDoNothing().given(authService).sendSignupEmailCode(anyString());

        mockMvc.perform(post("/auth/email/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("auth-email-send-code-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("email").description("인증 코드를 받을 이메일")
                        )
                ));
    }

    @Test
    @DisplayName("[회원가입 이메일 인증코드 검증]")
    void verifySignupEmailCode() throws Exception {
        EmailVerifyCodeRequest request = new EmailVerifyCodeRequest(TEST_EMAIL, "123456");

        willDoNothing().given(authService).verifySignupEmailCode(anyString(), anyString());

        mockMvc.perform(post("/auth/email/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("auth-email-verify-code-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("email").description("인증할 이메일"),
                                fieldWithPath("code").description("수신된 6자리 인증 코드")
                        )
                ));
    }

}