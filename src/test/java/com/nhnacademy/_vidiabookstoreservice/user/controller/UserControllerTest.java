package com.nhnacademy._vidiabookstoreservice.user.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.user.domain.Address;
import com.nhnacademy._vidiabookstoreservice.user.domain.Grade;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.dto.auth.request.CompleteProfileRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.ChangePasswordRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.DeleteUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.request.UpdateUserRequest;
import com.nhnacademy._vidiabookstoreservice.user.repository.AddressRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.GradeRepository;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest extends SupportControllerTest {

    @Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired private UserRepository userRepository;
    @Autowired private GradeRepository gradeRepository;
    @Autowired private AddressRepository addressRepository;

    private Long testUserId;
    private final String TEST_EMAIL = "user1@naver.com";

    @BeforeEach
    void initData() {

        Grade grade = gradeRepository.save(Grade.builder().gradeName(GradeName.WELCOME).pointRate(1).build());

        User user = User.builder()
                .email(TEST_EMAIL)
                .password(bCryptPasswordEncoder.encode("1234qwer!"))
                .name("유저1")
                .phone("01012345678")
                .birthDate(LocalDate.now())
                .grade(grade)
                .build();
        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);
        this.testUserId = savedUser.getUserId();

        Address address = Address.builder()
                .user(savedUser)
                .alias("집")
                .roadAddress("서울")
                .zipCode("12345")
                .addressDetail("101")
                .build();
        Address savedAddress = addressRepository.save(address);

        savedUser.setDefaultAddress(savedAddress);
        userRepository.save(savedUser);
    }

    @Test
    @DisplayName("[회원 조회 by email]")
    void getUserByEmail() throws Exception {
        this.mockMvc.perform(get("/users")
                        .param("email", TEST_EMAIL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("email").value(TEST_EMAIL))
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
        mockMvc.perform(get("/users/id")
                        .header("X-User-Id", testUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("userId").value(testUserId))
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
    @DisplayName("[회원 존재여부 조회]")
    void exists() throws Exception {
        mockMvc.perform(get("/users/{userId}/exists", testUserId))
                .andExpect(status().isOk())
                .andDo(document("user-exists-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        pathParameters(
                                parameterWithName("userId").description("존재 여부를 확인할 회원 식별 ID (PK)")
                        )
                ));

    }

    @Test
    @DisplayName("[회원 이름 조회]")
    void getUserName() throws Exception {
        mockMvc.perform(get("/users/name")
                        .header("X-User-Id", testUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("유저1"))
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
        mockMvc.perform(get("/users/profile")
                        .header("X-User-Id", testUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
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
        UpdateUserRequest request = new UpdateUserRequest("루저1", "01012345678");

        mockMvc.perform(put("/users/profile")
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
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
        ChangePasswordRequest request = new ChangePasswordRequest(
                "1234qwer!",
                "abcd5678!@",
                "abcd5678!@"
        );

        mockMvc.perform(put("/users/me/password")
                        .header("X-User-Id", testUserId)
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
        DeleteUserRequest request = new DeleteUserRequest("1234qwer!");

        mockMvc.perform(put("/users/delete")
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andDo(document("user-me-delete",
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
        mockMvc.perform(get("/users/role")
                        .header("X-User-Id", testUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("USER"))
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
        CompleteProfileRequest request = new CompleteProfileRequest(
                "update_email@payco.com",
                "update_name",
                "01011223344",
                LocalDate.now().minusYears(20)
        );

        mockMvc.perform(put("/users/complete-profile")
                        .header("X-User-Id", testUserId)
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