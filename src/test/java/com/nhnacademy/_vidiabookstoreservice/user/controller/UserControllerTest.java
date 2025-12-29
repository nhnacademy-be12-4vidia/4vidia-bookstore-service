package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.dto.address.response.AddressResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.CompleteProfileRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.ChangePasswordRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.DeleteUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.UpdateUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.UserInfoResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.UserProfileResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends SupportControllerTest {

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("[이메일로 회원 조회]")
    void getUserByEmail() throws Exception {
        UserInfoResponse response = new UserInfoResponse(1L, "test@example.com", "enc_password", "ROLE_USER");
        given(userService.getUserByEmail(anyString())).willReturn(response);

        mockMvc.perform(get("/users")
                        .param("email", "test@example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andDo(document("user-get-by-email",
                        queryParameters(parameterWithName("email").description("조회할 이메일")),
                        responseFields(withHeader(
                                fieldWithPath("data.id").description("회원 고유 ID"),
                                fieldWithPath("data.email").description("이메일"),
                                fieldWithPath("data.password").description("암호화된 비밀번호"),
                                fieldWithPath("data.roles").description("권한 (ROLE_USER, ROLE_ADMIN)")
                        ))
                ));
    }

    @Test
    @DisplayName("[ID로 회원 조회]")
    void getUserById() throws Exception {
        Long userId = 1L;
        // User 엔티티 모킹 (Service에서 User 객체를 반환하므로)
        User mockUser = mock(User.class);
        given(mockUser.getUserId()).willReturn(userId);
        given(mockUser.getEmail()).willReturn("test@example.com");
        given(mockUser.getName()).willReturn("홍길동");
        given(mockUser.getPhone()).willReturn("01012345678");
        given(mockUser.getBirthDate()).willReturn(LocalDate.of(1990, 1, 1));
        given(mockUser.getPoint()).willReturn(1000);

        // Grade 모킹 필요 (UserProfileResponse.fromEntity 내부에서 사용)
        Grade mockGrade = mock(Grade.class);
        given(mockGrade.getGradeName()).willReturn(GradeName.WELCOME);
        given(mockUser.getGrade()).willReturn(mockGrade);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(userService.getUserById(userId)).willReturn(mockUser);

            mockMvc.perform(get("/users/id")
                            .header("X-User-Id", userId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value(userId))
                    .andDo(document("user-get-by-id",
                            preprocessResponse(prettyPrint()),
                            requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                            responseFields(withHeader(
                                    fieldWithPath("data.userId").description("회원 ID"),
                                    fieldWithPath("data.email").description("이메일"),
                                    fieldWithPath("data.name").description("이름"),
                                    fieldWithPath("data.phone").description("전화번호"),
                                    fieldWithPath("data.birthDate").description("생년월일"),
                                    fieldWithPath("data.point").description("보유 포인트"),
                                    fieldWithPath("data.defaultAddress").description("기본 배송지 (없을 시 null)"),
                                    fieldWithPath("data.gradeName").description("회원 등급")
                            ))
                    ));
        }
    }

    @Test
    @DisplayName("[회원 존재 여부 확인]")
    void exists() throws Exception {
        Long userId = 1L;
        // 서비스에서 예외가 발생하지 않으면 존재하는 것으로 간주
        given(userService.getUserById(userId)).willReturn(mock(User.class));

        mockMvc.perform(get("/users/{userId}/exists", userId))
                .andExpect(status().isOk())
                .andDo(document("user-exists",
                        pathParameters(
                                parameterWithName("userId").description("확인할 회원 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[회원 이름 조회]")
    void getUserName() throws Exception {
        Long userId = 1L;
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(userService.getUserName(userId)).willReturn("홍길동");

            mockMvc.perform(get("/users/name")
                            .header("X-User-Id", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value("홍길동"))
                    .andDo(document("user-get-name",
                            responseFields(withHeader(fieldWithPath("data").description("회원 이름")))
                    ));
        }
    }

    @Test
    @DisplayName("[마이페이지 프로필 조회]")
    void getUserProfile() throws Exception {
        Long userId = 1L;
        AddressResponse addr = new AddressResponse(10L, "집", "도로명주소", "12345", "상세주소");
        UserProfileResponse response = new UserProfileResponse(
                userId, "test@example.com", "홍길동", "01012345678",
                LocalDate.of(1990, 1, 1), 5000, addr, "PLATINUM"
        );

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(userService.getUserInfo(userId)).willReturn(response);

            mockMvc.perform(get("/users/profile")
                            .header("X-User-Id", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("홍길동"))
                    .andDo(document("user-get-profile",
                            responseFields(withHeader(
                                    fieldWithPath("data.userId").description("회원 ID"),
                                    fieldWithPath("data.email").description("이메일"),
                                    fieldWithPath("data.name").description("이름"),
                                    fieldWithPath("data.phone").description("전화번호"),
                                    fieldWithPath("data.birthDate").description("생년월일"),
                                    fieldWithPath("data.point").description("보유 포인트"),
                                    fieldWithPath("data.defaultAddress").description("기본 배송지 정보"),
                                    fieldWithPath("data.defaultAddress.addressId").description("주소 ID"),
                                    fieldWithPath("data.defaultAddress.alias").description("주소 별칭"),
                                    fieldWithPath("data.defaultAddress.roadAddress").description("도로명 주소"),
                                    fieldWithPath("data.defaultAddress.zipCode").description("우편번호"),
                                    fieldWithPath("data.defaultAddress.addressDetail").description("상세 주소"),
                                    fieldWithPath("data.gradeName").description("회원 등급명")
                            ))
                    ));
        }
    }

    @Test
    @DisplayName("[프로필 정보 수정]")
    void updateUserProfile() throws Exception {
        Long userId = 1L;
        UpdateUserRequest request = new UpdateUserRequest("김철수", "01099998888");
        UserProfileResponse response = new UserProfileResponse(
                userId, "test@example.com", "김철수", "01099998888",
                LocalDate.of(1990, 1, 1), 5000, null, "GOLD"
        );

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(userService.updateUserProfile(eq(userId), any(UpdateUserRequest.class))).willReturn(response);

            mockMvc.perform(put("/users/profile")
                            .header("X-User-Id", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("김철수"))
                    .andDo(document("user-update-profile",
                            requestFields(
                                    fieldWithPath("name").description("변경할 이름"),
                                    fieldWithPath("phone").description("변경할 전화번호")
                            ),
                            responseFields(withHeader(
                                    fieldWithPath("data.userId").description("회원 ID"),
                                    fieldWithPath("data.name").description("변경된 이름"),
                                    fieldWithPath("data.phone").description("변경된 전화번호"),
                                    fieldWithPath("data.email").ignored(),
                                    fieldWithPath("data.birthDate").ignored(),
                                    fieldWithPath("data.point").ignored(),
                                    fieldWithPath("data.defaultAddress").ignored(),
                                    fieldWithPath("data.gradeName").ignored()
                            ))
                    ));
        }
    }

    @Test
    @DisplayName("[비밀번호 변경]")
    void changePassword() throws Exception {
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass123!", "newPass123!", "newPass123!");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            mockMvc.perform(put("/users/me/password")
                            .header("X-User-Id", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(document("user-change-password",
                            requestFields(
                                    fieldWithPath("currentPassword").description("현재 비밀번호"),
                                    fieldWithPath("newPassword").description("새 비밀번호"),
                                    fieldWithPath("confirmPassword").description("새 비밀번호 확인")
                            ),
                            responseFields(withHeader())
                    ));
        }
    }

    @Test
    @DisplayName("[회원 탈퇴]")
    void deleteUser() throws Exception {
        Long userId = 1L;
        DeleteUserRequest request = new DeleteUserRequest("password123!");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            mockMvc.perform(put("/users/delete")
                            .header("X-User-Id", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(document("user-delete",
                            requestFields(fieldWithPath("currentPassword").description("본인 확인용 비밀번호")),
                            responseFields(withHeader())
                    ));
        }
    }

    @Test
    @DisplayName("[회원 역할 조회]")
    void getUserRole() throws Exception {
        Long userId = 1L;
        String role = "ROLE_USER";

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            given(userService.getUserRole(userId)).willReturn(role);

            mockMvc.perform(get("/users/role")
                            .header("X-User-Id", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(role))
                    .andDo(document("user-get-role",
                            preprocessResponse(prettyPrint()),
                            requestHeaders(headerWithName("X-User-Id").description("회원 고유 ID")),
                            responseFields(withHeader(
                                    fieldWithPath("data").description("회원 권한 정보 (예: ROLE_USER, ROLE_ADMIN)")
                            ))
                    ));
        }
    }

    @Test
    @DisplayName("[페이코 필수 정보 입력]")
    void updateCompleteProfile() throws Exception {
        Long userId = 1L;
        CompleteProfileRequest request = new CompleteProfileRequest(
                "payco@test.com", "페이코유저", "01011112222", LocalDate.of(1995, 5, 5)
        );

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            UserContext mockContext = mock(UserContext.class);
            given(mockContext.getUserId()).willReturn(userId);
            mockedUserContext.when(UserContext::get).thenReturn(mockContext);

            mockMvc.perform(put("/users/complete-profile")
                            .header("X-User-Id", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(document("user-complete-profile",
                            requestFields(
                                    fieldWithPath("email").description("이메일"),
                                    fieldWithPath("name").description("이름"),
                                    fieldWithPath("phone").description("전화번호"),
                                    fieldWithPath("birthDate").description("생년월일")
                            ),
                            responseFields(withHeader())
                    ));
        }
    }
}