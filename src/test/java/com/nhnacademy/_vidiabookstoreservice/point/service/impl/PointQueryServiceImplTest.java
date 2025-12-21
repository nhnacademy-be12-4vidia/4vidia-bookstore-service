package com.nhnacademy._vidiabookstoreservice.point.service.impl;

import com.nhnacademy._vidiabookstoreservice.admin.domain.PointPolicy;
import com.nhnacademy._vidiabookstoreservice.point.domain.PointDetail;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import com.nhnacademy._vidiabookstoreservice.point.dto.response.PointHistoryResponse;
import com.nhnacademy._vidiabookstoreservice.point.repository.PointDetailRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointQueryServiceImplTest {

    @Mock
    private PointDetailRepository pointDetailRepository;

    @InjectMocks
    private PointQueryServiceImpl pointQueryService;

    @Test
    @DisplayName("포인트 내역 조회: category=null -> ALL 쿼리 + createdAt DESC pageable + DTO 매핑 검증")
    void getHistory_nullCategory_allQuery_andMapsDto() {
        // given
        Long userId = 1L;
        int page = 0, size = 10;

        LocalDateTime createdAt = LocalDateTime.of(2025, 12, 1, 10, 0);
        LocalDate expiredDate = LocalDate.of(2026, 1, 1);

        PointDetail detail = mock(PointDetail.class);
        when(detail.getCreatedAt()).thenReturn(createdAt);
        when(detail.getPrice()).thenReturn(500);
        when(detail.getReason()).thenReturn(PointReason.ORDER_REWARD);
        when(detail.getPointPolicy()).thenReturn(null);
        when(detail.getExpiredDate()).thenReturn(expiredDate);

        when(pointDetailRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(detail)));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        // when
        Page<PointHistoryResponse> result = pointQueryService.getHistory(userId, null, page, size);

        // then - repo 분기 검증
        verify(pointDetailRepository).findByUserIdOrderByCreatedAtDesc(eq(userId), pageableCaptor.capture());
        verify(pointDetailRepository, never())
                .findByUserIdAndPriceGreaterThanOrderByCreatedAtDesc(anyLong(), anyInt(), any());
        verify(pointDetailRepository, never())
                .findByUserIdAndPriceLessThanOrderByCreatedAtDesc(anyLong(), anyInt(), any());

        // then - pageable(createdAt desc) 검증
        Pageable pageable = pageableCaptor.getValue();
        assertEquals(page, pageable.getPageNumber());
        assertEquals(size, pageable.getPageSize());
        Sort.Order sortOrder = pageable.getSort().getOrderFor("createdAt");
        assertNotNull(sortOrder);
        assertEquals(Sort.Direction.DESC, sortOrder.getDirection());

        // then - DTO 매핑 검증
        assertEquals(1, result.getTotalElements());
        PointHistoryResponse dto = result.getContent().get(0);

        assertEquals(createdAt, dto.createdAt());
        assertEquals(500, dto.price());
        assertEquals(PointReason.ORDER_REWARD.getTitle(), dto.reason());
        assertNull(dto.policyName());
        assertEquals(expiredDate, dto.expiredDate());

        // record는 equals 자동이라, 이렇게도 가능:
        PointHistoryResponse expected = new PointHistoryResponse(
                createdAt, 500, PointReason.ORDER_REWARD.getTitle(), null, expiredDate
        );
        assertEquals(expected, dto);
    }

    @Test
    @DisplayName("포인트 내역 조회: category=EARN(대소문자 무관) -> price>0 쿼리 + policyName 매핑")
    void getHistory_earn_callsEarnQuery_andMapsPolicyName() {
        // given
        Long userId = 1L;

        LocalDateTime createdAt = LocalDateTime.of(2025, 12, 2, 10, 0);
        LocalDate expiredDate = LocalDate.of(2026, 1, 2);

        PointPolicy policy = mock(PointPolicy.class);
        when(policy.getPointName()).thenReturn("회원가입 적립");

        PointDetail detail = mock(PointDetail.class);
        when(detail.getCreatedAt()).thenReturn(createdAt);
        when(detail.getPrice()).thenReturn(100);
        when(detail.getReason()).thenReturn(PointReason.POLICY_REWARD);
        when(detail.getPointPolicy()).thenReturn(policy);
        when(detail.getExpiredDate()).thenReturn(expiredDate);

        when(pointDetailRepository.findByUserIdAndPriceGreaterThanOrderByCreatedAtDesc(eq(userId), eq(0), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(detail)));

        // when
        Page<PointHistoryResponse> result = pointQueryService.getHistory(userId, "eArN", 0, 10);

        // then
        verify(pointDetailRepository).findByUserIdAndPriceGreaterThanOrderByCreatedAtDesc(eq(userId), eq(0), any(Pageable.class));
        verify(pointDetailRepository, never()).findByUserIdAndPriceLessThanOrderByCreatedAtDesc(anyLong(), anyInt(), any());
        verify(pointDetailRepository, never()).findByUserIdOrderByCreatedAtDesc(anyLong(), any());

        PointHistoryResponse dto = result.getContent().get(0);
        assertEquals(createdAt, dto.createdAt());
        assertEquals(100, dto.price());
        assertEquals(PointReason.POLICY_REWARD.getTitle(), dto.reason());
        assertEquals("회원가입 적립", dto.policyName());
        assertEquals(expiredDate, dto.expiredDate());
    }

    @Test
    @DisplayName("포인트 내역 조회: category=USE -> price<0 쿼리 호출")
    void getHistory_use_callsUseQuery() {
        // given
        Long userId = 1L;

        LocalDateTime createdAt = LocalDateTime.of(2025, 12, 3, 10, 0);

        PointDetail detail = mock(PointDetail.class);
        when(detail.getCreatedAt()).thenReturn(createdAt);
        when(detail.getPrice()).thenReturn(-300);
        when(detail.getReason()).thenReturn(PointReason.ORDER_USE);
        when(detail.getPointPolicy()).thenReturn(null);
        when(detail.getExpiredDate()).thenReturn(null);

        when(pointDetailRepository.findByUserIdAndPriceLessThanOrderByCreatedAtDesc(eq(userId), eq(0), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(detail)));

        // when
        Page<PointHistoryResponse> result = pointQueryService.getHistory(userId, "USE", 0, 10);

        // then
        verify(pointDetailRepository).findByUserIdAndPriceLessThanOrderByCreatedAtDesc(eq(userId), eq(0), any(Pageable.class));
        verify(pointDetailRepository, never()).findByUserIdAndPriceGreaterThanOrderByCreatedAtDesc(anyLong(), anyInt(), any());
        verify(pointDetailRepository, never()).findByUserIdOrderByCreatedAtDesc(anyLong(), any());

        PointHistoryResponse dto = result.getContent().get(0);
        assertEquals(createdAt, dto.createdAt());
        assertEquals(-300, dto.price());
        assertEquals(PointReason.ORDER_USE.getTitle(), dto.reason());
        assertNull(dto.policyName());
        assertNull(dto.expiredDate());
    }

    @Test
    @DisplayName("포인트 내역 조회: 알 수 없는 category -> default(ALL) 쿼리 호출")
    void getHistory_unknownCategory_defaultsToAll() {
        // given
        Long userId = 1L;

        when(pointDetailRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
                .thenReturn(Page.empty());

        // when
        Page<PointHistoryResponse> result = pointQueryService.getHistory(userId, "SOMETHING", 0, 10);

        // then
        verify(pointDetailRepository).findByUserIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class));
        assertTrue(result.isEmpty());
    }
}
