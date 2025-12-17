package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.CompleteProfileRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.ChangePasswordRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.DeleteUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.UpdateUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.response.UserProfileResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureRestDocs
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@ActiveProfiles("local")
@SpringBootTest
@Transactional
class UserControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    @DisplayName("[회원 조회 by email]")
    void getUserByEmail() throws Exception {
        String email = "user1@naver.com";

        mockMvc.perform(get("/users")
                        .param("email", email)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").isNumber())
                .andExpect(jsonPath("email").isString())
                .andExpect(jsonPath("password").isString())
                .andExpect(jsonPath("roles").isString())
                .andDo(document("user-by-email-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        queryParameters(
                                parameterWithName("email").description("조회할 사용자 이메일")
                        ),
                        responseFields(
                                fieldWithPath("id").description("유저 아이디"),
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("password").description("비밀번호"),
                                fieldWithPath("roles").description("권한 (USER, ADMIN)")
                        )
                ));
    }

    @Test
    @DisplayName("[회원 조회 by id]")
    void getUserById() throws Exception {
        Long userId = 14L;

        mockMvc.perform(get("/users/id")
                        .header("X-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("userId").isNumber())
                .andExpect(jsonPath("email").isString())
                .andExpect(jsonPath("name").isString())
                .andExpect(jsonPath("phone").isString())
                .andExpect(jsonPath("birthDate").isString())
                .andExpect(jsonPath("point").isNumber())
                .andExpect(jsonPath("defaultAddress").exists())
                .andExpect(jsonPath("gradeName").isString())
                .andDo(document("user-by-id-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        responseFields(
                                fieldWithPath("userId").description("회원 PK"),
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("name").description("이름"),
                                fieldWithPath("phone").description("전화번호"),
                                fieldWithPath("birthDate").description("생년월일"),
                                fieldWithPath("point").description("포인트"),
                                fieldWithPath("defaultAddress").description("기본 주소"),

                                fieldWithPath("defaultAddress.addressId").description("주소 PK"),
                                fieldWithPath("defaultAddress.alias").description("별칭"),
                                fieldWithPath("defaultAddress.roadAddress").description("도로명주소"),
                                fieldWithPath("defaultAddress.zipCode").description("우편번호"),
                                fieldWithPath("defaultAddress.addressDetail").description("상세주소"),

                                fieldWithPath("gradeName").description("회원 등급")
                        )
                ));
    }

    @Test
    @DisplayName("[회원 이름 조회]")
    void getUserName() throws Exception {
        Long userId = 14L;
        String expectedName = "유저1";

        mockMvc.perform(get("/users/name")
                    .header("X-User-Id", userId)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedName))
                .andDo(document("user-name-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[회원 프로필 조회]")
    void getUserProfile() throws Exception {
        Long userId = 14L;

        mockMvc.perform(get("/users/profile")
                    .header("X-User-Id", userId)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("userId").isNumber())
                .andExpect(jsonPath("email").isString())
                .andExpect(jsonPath("name").isString())
                .andExpect(jsonPath("phone").isString())
                .andExpect(jsonPath("birthDate").isString())
                .andExpect(jsonPath("point").isNumber())
                .andExpect(jsonPath("defaultAddress").exists())
                .andExpect(jsonPath("gradeName").isString())
                .andDo(document("user-profile-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        responseFields(
                                fieldWithPath("userId").description("회원 PK"),
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("name").description("이름"),
                                fieldWithPath("phone").description("전화번호"),
                                fieldWithPath("birthDate").description("생년월일"),
                                fieldWithPath("point").description("포인트"),
                                fieldWithPath("defaultAddress").description("기본 주소"),

                                fieldWithPath("defaultAddress.addressId").description("주소 PK"),
                                fieldWithPath("defaultAddress.alias").description("별칭"),
                                fieldWithPath("defaultAddress.roadAddress").description("도로명주소"),
                                fieldWithPath("defaultAddress.zipCode").description("우편번호"),
                                fieldWithPath("defaultAddress.addressDetail").description("상세주소"),

                                fieldWithPath("gradeName").description("회원 등급")
                        )
                ));
    }

    @Test
    @DisplayName("[회원 프로필 수정]")
    void updateUserProfile() throws Exception {
        Long userId = 14L;

        UpdateUserRequest request = new UpdateUserRequest("루저1", "01012345678");

        mockMvc.perform(put("/users/profile")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("userId").isNumber())
                .andExpect(jsonPath("email").isString())
                .andExpect(jsonPath("name").value(request.name()))
                .andExpect(jsonPath("phone").value(request.phone()))
                .andExpect(jsonPath("birthDate").isString())
                .andExpect(jsonPath("point").isNumber())
                .andExpect(jsonPath("defaultAddress").exists())
                .andExpect(jsonPath("gradeName").isString())
                .andDo(document("user-profile-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        requestFields(
                                fieldWithPath("name").description("변경할 이름"),
                                fieldWithPath("phone").description("변경할 전화번호")
                        ),
                        responseFields(
                                fieldWithPath("userId").description("회원 PK"),
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("name").description("수정된 이름"),
                                fieldWithPath("phone").description("수정된 전화번호"),
                                fieldWithPath("birthDate").description("생년월일"),
                                fieldWithPath("point").description("포인트"),
                                fieldWithPath("defaultAddress").description("기본 주소"),

                                fieldWithPath("defaultAddress.addressId").description("주소 PK"),
                                fieldWithPath("defaultAddress.alias").description("별칭"),
                                fieldWithPath("defaultAddress.roadAddress").description("도로명주소"),
                                fieldWithPath("defaultAddress.zipCode").description("우편번호"),
                                fieldWithPath("defaultAddress.addressDetail").description("상세주소"),

                                fieldWithPath("gradeName").description("회원 등급")
                        )
                ));
    }

    @Test
    @DisplayName("[회원 비밀번호 수정]")
    void changePassword() throws Exception {
        Long userId = 14L;

        ChangePasswordRequest request = new ChangePasswordRequest(
                "1234qwer!",
                "abcd5678!@",
                "abcd5678!@"
        );

        mockMvc.perform(put("/users/me/password")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andDo(document("user-password-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        requestFields(
                                fieldWithPath("currentPassword").description("현재 비밀번호"),
                                fieldWithPath("newPassword").description("새로운 비밀번호"),
                                fieldWithPath("confirmPassword").description("비밀번호 확인")
                        )
                ));
    }

    @Test
    @DisplayName("[회원 탈퇴]")
    void deleteUser() throws Exception {
        Long userId = 14L;

        DeleteUserRequest request = new DeleteUserRequest("1234qwer!");

        mockMvc.perform(put("/users/delete")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andDo(document("user-me-delete", // 실제 = put맵핑 (db에서 삭제안함)
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        requestFields(
                                fieldWithPath("currentPassword").description("현재 비밀번호")
                        )
                ));
    }

    @Test
    @DisplayName("[회원 권한 조회]")
    void getUserRole() throws Exception {
        Long userId = 14L;
        String expectedRole = "USER";

        mockMvc.perform(get("/users/role")
                        .header("X-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedRole))
                .andDo(document("user-role-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[Payco 로그인 후 필수 정보 수정]")
    void updateCompleteProfile() throws Exception {
        Long userId = 14L;

        CompleteProfileRequest request = new CompleteProfileRequest(
                "update_email@payco.com",
                "update_name",
                "01011223344",
                LocalDate.now().minusYears(20)
        );


        mockMvc.perform(put("/users/complete-profile")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("user-payco-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestHeaders(
                                headerWithName("X-User-Id").description("사용자 식별 ID")
                        ),
                        requestFields(
                                fieldWithPath("email").description("payco계정 이메일"),
                                fieldWithPath("name").description("payco계정 이름"),
                                fieldWithPath("phone").description("payco계정 전화번호"),
                                fieldWithPath("birthDate").description("payco계정 생년월일")
                        )
                ));
    }
}