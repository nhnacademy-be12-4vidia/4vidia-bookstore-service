package com.nhnacademy._vidiabookstoreservice.admin.controller;

import com.nhnacademy._vidiabookstoreservice.SupportControllerTest;
import com.nhnacademy._vidiabookstoreservice.admin.dto.AdminUserSearchCondition;
import com.nhnacademy._vidiabookstoreservice.admin.dto.request.UpdateUserStatusRequest;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminUserResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.AdminUserService;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
class AdminUserControllerTest extends SupportControllerTest {

    @MockitoBean
    private AdminUserService adminUserService;

    @Test
    @DisplayName("GET - 회원 목록 조회 (검색 + 페이징)")
    void getUsers() throws Exception {
        AdminUserResponse userResponse = new AdminUserResponse(
                1L,
                "test@example.com",
                "홍길동",
                "010-1234-5678",
                LocalDate.of(1990, 1, 1),
                1000,
                UserStatus.ACTIVE,
                LocalDate.now(),
                LocalDateTime.now(),
                "WELCOME"
        );
        Page<AdminUserResponse> page = new PageImpl<>(List.of(userResponse), PageRequest.of(0, 10), 1);

        given(adminUserService.getUsers(any(AdminUserSearchCondition.class), any(Pageable.class)))
                .willReturn(page);

        mockMvc.perform(get("/admin/users")
                        .param("keyword", "홍길동")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].email").value("test@example.com"))
                .andDo(document("admin-users-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("keyword").description("검색어 (이메일 또는 이름)").optional(),
                                parameterWithName("status").description("회원 상태 (ACTIVE, DORMANT, DELETED)").optional(),
                                parameterWithName("page").description("페이지 번호").optional(),
                                parameterWithName("size").description("페이지 크기").optional()
                        ),
                        responseFields(withHeader(
                                fieldWithPath("data.content[].userId").description("회원 ID"),
                                fieldWithPath("data.content[].email").description("이메일"),
                                fieldWithPath("data.content[].name").description("이름"),
                                fieldWithPath("data.content[].phone").description("전화번호"),
                                fieldWithPath("data.content[].birthDate").description("생년월일"),
                                fieldWithPath("data.content[].point").description("보유 포인트"),
                                fieldWithPath("data.content[].status").description("상태"),
                                fieldWithPath("data.content[].joinedAt").description("가입일"),
                                fieldWithPath("data.content[].lastLoginAt").description("마지막 로그인 일시"),
                                fieldWithPath("data.content[].gradeName").description("회원 등급"),

                                fieldWithPath("data.page").description("현재 페이지 번호"),
                                fieldWithPath("data.size").description("페이지 크기"),
                                fieldWithPath("data.totalElements").description("전체 데이터 수"),
                                fieldWithPath("data.totalPages").description("전체 페이지 수"),
                                fieldWithPath("data.last").description("마지막 페이지 여부")
                        ))
                ));
    }

    @Test
    @DisplayName("GET - 회원 상세 조회")
    void getUser() throws Exception {
        Long userId = 1L;
        AdminUserResponse response = new AdminUserResponse(
                userId,
                "test@example.com",
                "홍길동",
                "010-1234-5678",
                LocalDate.of(1990, 1, 1),
                5000,
                UserStatus.ACTIVE,
                LocalDate.now().minusDays(10),
                LocalDateTime.now(),
                "PLATINUM"
        );

        given(adminUserService.getUser(userId)).willReturn(response);

        mockMvc.perform(get("/admin/users/{userId}", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andDo(document("admin-user-detail-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("userId").description("조회할 회원 ID")
                        ),
                        responseFields(withHeader(
                                fieldWithPath("data.userId").description("회원 ID"),
                                fieldWithPath("data.email").description("이메일"),
                                fieldWithPath("data.name").description("이름"),
                                fieldWithPath("data.phone").description("전화번호"),
                                fieldWithPath("data.birthDate").description("생년월일"),
                                fieldWithPath("data.point").description("보유 포인트"),
                                fieldWithPath("data.status").description("상태"),
                                fieldWithPath("data.joinedAt").description("가입일"),
                                fieldWithPath("data.lastLoginAt").description("마지막 로그인 일시"),
                                fieldWithPath("data.gradeName").description("회원 등급")
                        ))
                ));
    }

    @Test
    @DisplayName("PUT - 회원 상태 변경")
    void updateUserStatus() throws Exception {
        Long userId = 1L;
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(UserStatus.DORMANT);

        doNothing().when(adminUserService).updateUserStatus(eq(userId), eq(UserStatus.DORMANT));

        mockMvc.perform(put("/admin/users/{userId}/status", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("admin-user-status-update-put",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("userId").description("상태 변경할 회원 ID")
                        ),
                        requestFields(
                                fieldWithPath("status").description("변경할 상태 (ACTIVE, DORMANT, DELETED)")
                        ),
                        responseFields(withHeader())
                ));
    }
}