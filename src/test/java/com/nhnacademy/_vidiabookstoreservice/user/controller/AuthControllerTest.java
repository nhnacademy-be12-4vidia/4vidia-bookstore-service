package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.*;
import com.nhnacademy._vidiabookstoreservice.user.dto.dormant.request.DormantSendCodeByEmailRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.dormant.request.DormantSendCodeRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.dormant.request.DormantVerifyRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.UpdateLastLoginRequest;
import com.nhnacademy._vidiabookstoreservice.user.service.AuthService;
import com.nhnacademy._vidiabookstoreservice.user.service.DormantAuthService;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest extends SupportControllerTest {

    @MockitoBean private AuthService authService;
    @MockitoBean private UserService userService;
    @MockitoBean private DormantAuthService dormantAuthService;

    @Test
    @Order(1)
    @DisplayName("POST - 회원가입 요청")
    void signup() throws Exception {
        UserSignupRequest request = new UserSignupRequest(
                "test@example.com", "password123!", "홍길동", "01012345678", LocalDate.of(1990, 1, 1)
        );
        given(authService.register(any(UserSignupRequest.class))).willReturn(1L);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value(1L))
                .andDo(document("auth-signup-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").description("이메일 (아이디)"),
                                fieldWithPath("password").description("비밀번호 (소문자, 숫자, 특수문자 포함 8~20자)"),
                                fieldWithPath("name").description("이름"),
                                fieldWithPath("phone").description("전화번호 (숫자만)"),
                                fieldWithPath("birthDate").description("생년월일 (yyyy-MM-dd)")
                        ),
                        responseFields(withHeader(fieldWithPath("data").description("생성된 회원 ID")))
                ));
    }

    @Test
    @Order(2)
    @DisplayName("GET - 회원가입 이메일 중복 확인")
    void checkEmail() throws Exception {
        given(authService.existsByEmail(anyString())).willReturn(true);

        mockMvc.perform(get("/auth/emails/exists")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true))
                .andDo(document("auth-email-exists-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(parameterWithName("email").description("중복 확인할 이메일")),
                        responseFields(withHeader(fieldWithPath("data").description("중복 여부 (true/false)")))
                ));
    }

    @Test
    @Order(3)
    @DisplayName("POST - 회원가입 이메일 인증코드 전송")
    void sendSignupEmailCode() throws Exception {
        EmailSendCodeRequest request = new EmailSendCodeRequest("test@example.com");

        mockMvc.perform(post("/auth/email/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("auth-signup-email-send-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(fieldWithPath("email").description("인증받을 이메일")),
                        responseFields(withHeader())
                ));
    }

    @Test
    @Order(4)
    @DisplayName("POST - 회원가입 이메일 인증코드 검증")
    void verifySignupEmailCode() throws Exception {
        EmailVerifyCodeRequest request = new EmailVerifyCodeRequest("test@example.com", "654321");

        mockMvc.perform(post("/auth/email/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("auth-signup-email-verify-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("code").description("인증 코드")
                        ),
                        responseFields(withHeader())
                ));
    }

    @Test
    @Order(5)
    @DisplayName("PUT - 로그인 마지막 시간 업데이트")
    void updateLastLoginAt() throws Exception {
        UpdateLastLoginRequest request = new UpdateLastLoginRequest("test@example.com");

        mockMvc.perform(put("/auth/last-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("auth-update-last-login-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(fieldWithPath("email").description("회원 이메일")),
                        responseFields(withHeader())
                ));
    }


    @Test
    @Order(6)
    @DisplayName("POST - 로그인 아이디 찾기(email)")
    void findUserId() throws Exception {
        FindIdRequest request = new FindIdRequest("홍길동", "1990-01-01", "01012345678");
        given(authService.findUserId(any(FindIdRequest.class))).willReturn("te**@example.com");

        mockMvc.perform(post("/auth/find-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("te**@example.com"))
                .andDo(document("auth-find-id-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").description("이름"),
                                fieldWithPath("birthday").description("생년월일 (yyyy-MM-dd)"),
                                fieldWithPath("phone").description("전화번호")
                        ),
                        responseFields(withHeader(fieldWithPath("data").description("마스킹 처리된 이메일")))
                ));
    }

    @Test
    @Order(7)
    @DisplayName("POST - 로그인 비밀번호 초기화")
    void findPassword() throws Exception {
        FindPasswordRequest request = new FindPasswordRequest("test@example.com", "홍길동", "01012345678");

        given(authService.restPasswordAndSendMail(any(FindPasswordRequest.class))).willReturn(null);

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
                        ),
                        responseFields(withHeader())
                ));
    }

    @Test
    @Order(8)
    @DisplayName("GET - 휴면 여부 확인")
    void checkDormant() throws Exception {
        given(authService.isDormant(anyString())).willReturn(false);

        mockMvc.perform(get("/auth/dormant")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andDo(document("auth-check-dormant-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(parameterWithName("email").description("확인할 이메일")),
                        responseFields(withHeader(fieldWithPath("data").description("휴면 계정 여부")))
                ));
    }

    @Test
    @Order(9)
    @DisplayName("POST - 휴면 인증코드 전송(Webhook)")
    void sendDormantCode() throws Exception {
        DormantSendCodeRequest request = new DormantSendCodeRequest("test@example.com", "http://webhook.url");

        mockMvc.perform(post("/auth/dormant/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("auth-dormant-send-code-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("webhookUrl").description("인증코드를 받을 Webhook URL")
                        ),
                        responseFields(withHeader())
                ));
    }

    @Test
    @Order(10)
    @DisplayName("POST - 휴면 인증코드 전송(Email)")
    void sendDormantCodeByEmail() throws Exception {
        DormantSendCodeByEmailRequest request = new DormantSendCodeByEmailRequest(
                "dormant@example.com", "contact@example.com"
        );

        mockMvc.perform(post("/auth/dormant/send-code/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("auth-dormant-send-code-email-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").description("휴면 계정 이메일"),
                                fieldWithPath("contactEmail").description("인증 코드를 받을 연락처 이메일")
                        ),
                        responseFields(withHeader())
                ));
    }

    @Test
    @Order(11)
    @DisplayName("POST - 휴면 인증코드 검증")
    void verifyDormantCode() throws Exception {
        DormantVerifyRequest request = new DormantVerifyRequest("test@example.com", "123456");

        mockMvc.perform(post("/auth/dormant/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("auth-dormant-verify-post",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("code").description("인증 코드")
                        ),
                        responseFields(withHeader())
                ));
    }

}