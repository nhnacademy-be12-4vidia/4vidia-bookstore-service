package com.nhnacademy._vidiabookstoreservice.global.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.PaycoUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.response.AuthUserDto;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.response.OAuth2UserDto;
import com.nhnacademy._vidiabookstoreservice.user.service.AuthService;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InternalControllerTest extends SupportControllerTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("[내부 호출] 이메일로 인증용 유저 정보 조회")
    void getInternalUserByEmail() throws Exception {
        // Given
        String email = "test@example.com";
        AuthUserDto response = new AuthUserDto(1L, email, "encoded_password", "ROLE_USER", "ACTIVE");

        given(userService.getAuthUser(email)).willReturn(response);

        // When & Then
        mockMvc.perform(get("/internal/users")
                        .param("email", email)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andDo(document("internal-get-user-by-email",
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("email").description("조회할 유저 이메일")
                        ),
                        responseFields(
                                fieldWithPath("id").description("유저 고유 번호"),
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("password").description("인증용 암호화 비밀번호"),
                                fieldWithPath("roles").description("권한"),
                                fieldWithPath("status").description("유저 상태")
                        )
                ));
    }

    @Test
    @DisplayName("[내부 호출] 페이코 유저 조회 또는 생성")
    void findOrCreateByPaycoId() throws Exception {
        // Given
        PaycoUserRequest request = new PaycoUserRequest("payco_unique_id_123");
        OAuth2UserDto response = new OAuth2UserDto();
        response.setUserId(1L);
        response.setEmail("payco_user@example.com");
        response.setRole("ROLE_USER");
        response.setStatus("ACTIVE");

        given(authService.findOrCreateOAuthUser(eq("payco"), any(PaycoUserRequest.class))).willReturn(response);

        // When & Then
        mockMvc.perform(post("/internal/payco/find-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andDo(document("internal-payco-find-or-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("id").description("페이코에서 제공한 유저 고유 ID")
                        ),
                        responseFields(
                                fieldWithPath("userId").description("시스템 내부 유저 번호"),
                                fieldWithPath("email").description("유저 이메일"),
                                fieldWithPath("role").description("권한"),
                                fieldWithPath("status").description("계정 상태")
                        )
                ));
    }

    @Test
    @DisplayName("[내부 호출] 유저 존재 여부 확인")
    void exists() throws Exception {
        // Given
        Long userId = 1L;
        given(userService.getUserById(userId)).willReturn(mock(User.class));

        // When & Then
        mockMvc.perform(get("/internal/users/{userId}/exists", userId))
                .andExpect(status().isOk())
                .andDo(document("internal-user-exists",
                        pathParameters(
                                parameterWithName("userId").description("확인할 유저 번호")
                        )
                ));
    }
}