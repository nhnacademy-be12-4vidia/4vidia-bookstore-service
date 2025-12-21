package com.nhnacademy._vidiabookstoreservice.admin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.nhnacademy._vidiabookstoreservice.admin.dto.AdminUserSearchCondition;
import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminUserResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.impl.AdminUserServiceImpl;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import com.nhnacademy._vidiabookstoreservice.user.exception.notfound.UserNotFoundException;
import com.nhnacademy._vidiabookstoreservice.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    /**
     * User 엔티티 생성을 위한 공통 헬퍼 메서드
     */
    private User createTestUser(Long id, String email) {
        User user = User.builder() // @Builder(builderClassName = "LocalUserBuilder") 사용
                .email(email)
                .name("테스트유저")
                .password("password123!")
                .phone("010-1234-5678")
                .birthDate(LocalDate.of(1995, 1, 1))
                .build();

        // ID는 DB에서 생성되므로 Reflection을 통해 강제로 주입 (필요한 경우)
        ReflectionTestUtils.setField(user, "userId", id);
        return user;
    }

    @Test
    @DisplayName("사용자 목록 조회 - 검색 조건 및 페이징 검증")
    void getUsers_WithCondition() {
        // given
        AdminUserSearchCondition condition = new AdminUserSearchCondition("  vidiabook  ", UserStatus.ACTIVE);
        Pageable pageable = PageRequest.of(0, 10);

        User user = createTestUser(1L, "vidiabook@nhn.com");
        Page<User> userPage = new PageImpl<>(List.of(user));

        // keyword.trim()이 적용된 "vidiabook"이 전달되는지 검증
        given(userRepository.searchAdminUsers(UserStatus.ACTIVE, "vidiabook", pageable))
                .willReturn(userPage);

        // when
        Page<AdminUserResponse> result = adminUserService.getUsers(condition, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).email()).isEqualTo("vidiabook@nhn.com");
        verify(userRepository).searchAdminUsers(UserStatus.ACTIVE, "vidiabook", pageable);
    }

    @Test
    @DisplayName("단일 사용자 조회 - 성공 시 Response 변환 확인")
    void getUser_Success() {
        // given
        Long userId = 1L;
        User user = createTestUser(userId, "test@test.com");
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        AdminUserResponse response = adminUserService.getUser(userId);

        // then
        assertThat(response.email()).isEqualTo("test@test.com");
        assertThat(response.status()).isEqualTo(UserStatus.ACTIVE); // 빌더 기본값 확인
    }

    @Test
    @DisplayName("상태 업데이트 - Dirty Checking 검증")
    void updateUserStatus_Success() {
        // given
        Long userId = 1L;
        User user = createTestUser(userId, "active@test.com");
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        adminUserService.updateUserStatus(userId, UserStatus.DORMANT);

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.DORMANT);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("사용자 조회 실패 - UserNotFoundException 발생")
    void getUser_Fail_ThrowsUserNotFoundException() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminUserService.getUser(1L))
                .isInstanceOf(UserNotFoundException.class);
    }
}