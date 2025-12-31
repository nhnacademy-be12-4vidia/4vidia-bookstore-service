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
    @DisplayName("포인트 내역 조회: category=null -> ALL(기간필터) 쿼리 + createdAt DESC pageable + DTO 매핑 검증")
    void getHistory_nullCategory_allQuery_withDateRange_andMapsDto() {
        // given
        Long userId = 1L;
        int page = 0, size = 10;

        LocalDate from = LocalDate.of(2025, 12, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);

        LocalDateTime expectedStart = from.atStartOfDay();
        LocalDateTime expectedEndExclusive = to.plusDays(1).atStartOfDay();

        LocalDateTime createdAt = LocalDateTime.of(2025, 12, 1, 10, 0);
        LocalDate expiredDate = LocalDate.of(2026, 1, 1);

        PointDetail detail = mock(PointDetail.class);
        when(detail.getCreatedAt()).thenReturn(createdAt);
        when(detail.getPrice()).thenReturn(500);
        when(detail.getReason()).thenReturn(PointReason.ORDER_REWARD);
        when(detail.getPointPolicy()).thenReturn(null);
        when(detail.getExpiredDate()).thenReturn(expiredDate);

        when(pointDetailRepository
                .findByUserIdAndCreatedAtBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(detail)));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        // when
        Page<PointHistoryResponse> result = pointQueryService.getHistory(userId, null, from, to, page, size);

        // then - repo 분기 검증 + 기간 전달 검증
        verify(pointDetailRepository).findByUserIdAndCreatedAtBetween(
                eq(userId),
                fromCaptor.capture(),
                toCaptor.capture(),
                pageableCaptor.capture()
        );

        verify(pointDetailRepository, never())
                .findByUserIdAndCreatedAtBetweenAndPriceGreaterThan(anyLong(), any(), any(), anyInt(), any());
        verify(pointDetailRepository, never())
                .findByUserIdAndCreatedAtBetweenAndPriceLessThan(anyLong(), any(), any(), anyInt(), any());

        assertEquals(expectedStart, fromCaptor.getValue());
        assertEquals(expectedEndExclusive, toCaptor.getValue());

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

        PointHistoryResponse expected = new PointHistoryResponse(
                createdAt, 500, PointReason.ORDER_REWARD.getTitle(), null, expiredDate
        );
        assertEquals(expected, dto);
    }

    @Test
    @DisplayName("포인트 내역 조회: category=EARN(대소문자 무관) -> price>0(기간필터) 쿼리 + policyName 매핑")
    void getHistory_earn_callsEarnQuery_withDateRange_andMapsPolicyName() {
        // given
        Long userId = 1L;
        LocalDate from = LocalDate.of(2025, 12, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);

        LocalDateTime expectedStart = from.atStartOfDay();
        LocalDateTime expectedEndExclusive = to.plusDays(1).atStartOfDay();

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

        when(pointDetailRepository
                .findByUserIdAndCreatedAtBetweenAndPriceGreaterThan(eq(userId), any(), any(), eq(0), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(detail)));

        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        // when
        Page<PointHistoryResponse> result = pointQueryService.getHistory(userId, "eArN", from, to, 0, 10);

        // then
        verify(pointDetailRepository).findByUserIdAndCreatedAtBetweenAndPriceGreaterThan(
                eq(userId), fromCaptor.capture(), toCaptor.capture(), eq(0), any(Pageable.class)
        );

        assertEquals(expectedStart, fromCaptor.getValue());
        assertEquals(expectedEndExclusive, toCaptor.getValue());

        verify(pointDetailRepository, never())
                .findByUserIdAndCreatedAtBetweenAndPriceLessThan(anyLong(), any(), any(), anyInt(), any());
        verify(pointDetailRepository, never())
                .findByUserIdAndCreatedAtBetween(anyLong(), any(), any(), any());

        PointHistoryResponse dto = result.getContent().get(0);
        assertEquals(createdAt, dto.createdAt());
        assertEquals(100, dto.price());
        assertEquals(PointReason.POLICY_REWARD.getTitle(), dto.reason());
        assertEquals("회원가입 적립", dto.policyName());
        assertEquals(expiredDate, dto.expiredDate());
    }

    @Test
    @DisplayName("포인트 내역 조회: category=USE -> price<0(기간필터) 쿼리 호출")
    void getHistory_use_callsUseQuery_withDateRange() {
        // given
        Long userId = 1L;
        LocalDate from = LocalDate.of(2025, 12, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);

        LocalDateTime expectedStart = from.atStartOfDay();
        LocalDateTime expectedEndExclusive = to.plusDays(1).atStartOfDay();

        LocalDateTime createdAt = LocalDateTime.of(2025, 12, 3, 10, 0);

        PointDetail detail = mock(PointDetail.class);
        when(detail.getCreatedAt()).thenReturn(createdAt);
        when(detail.getPrice()).thenReturn(-300);
        when(detail.getReason()).thenReturn(PointReason.ORDER_USE);
        when(detail.getPointPolicy()).thenReturn(null);
        when(detail.getExpiredDate()).thenReturn(null);

        when(pointDetailRepository
                .findByUserIdAndCreatedAtBetweenAndPriceLessThan(eq(userId), any(), any(), eq(0), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(detail)));

        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        // when
        Page<PointHistoryResponse> result = pointQueryService.getHistory(userId, "USE", from, to, 0, 10);

        // then
        verify(pointDetailRepository).findByUserIdAndCreatedAtBetweenAndPriceLessThan(
                eq(userId), fromCaptor.capture(), toCaptor.capture(), eq(0), any(Pageable.class)
        );

        assertEquals(expectedStart, fromCaptor.getValue());
        assertEquals(expectedEndExclusive, toCaptor.getValue());

        verify(pointDetailRepository, never())
                .findByUserIdAndCreatedAtBetweenAndPriceGreaterThan(anyLong(), any(), any(), anyInt(), any());
        verify(pointDetailRepository, never())
                .findByUserIdAndCreatedAtBetween(anyLong(), any(), any(), any());

        PointHistoryResponse dto = result.getContent().get(0);
        assertEquals(createdAt, dto.createdAt());
        assertEquals(-300, dto.price());
        assertEquals(PointReason.ORDER_USE.getTitle(), dto.reason());
        assertNull(dto.policyName());
        assertNull(dto.expiredDate());
    }

    @Test
    @DisplayName("포인트 내역 조회: 알 수 없는 category -> default(ALL, 기간필터) 쿼리 호출")
    void getHistory_unknownCategory_defaultsToAll_withDateRange() {
        // given
        Long userId = 1L;
        LocalDate from = LocalDate.of(2025, 12, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);

        when(pointDetailRepository
                .findByUserIdAndCreatedAtBetween(eq(userId), any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        // when
        Page<PointHistoryResponse> result = pointQueryService.getHistory(userId, "SOMETHING", from, to, 0, 10);

        // then
        verify(pointDetailRepository)
                .findByUserIdAndCreatedAtBetween(eq(userId), any(), any(), any(Pageable.class));
        assertTrue(result.isEmpty());
    }
    @Test
    @DisplayName("포인트 내역 조회: from > to면 스왑해서 조회한다")
    void getHistory_swapsWhenFromAfterTo() {
        Long userId = 1L;
        LocalDate from = LocalDate.of(2025, 12, 31);
        LocalDate to = LocalDate.of(2025, 12, 1);

        when(pointDetailRepository.findByUserIdAndCreatedAtBetween(eq(userId), any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        pointQueryService.getHistory(userId, "ALL", from, to, 0, 10);

        verify(pointDetailRepository).findByUserIdAndCreatedAtBetween(
                eq(userId), fromCaptor.capture(), toCaptor.capture(), any(Pageable.class)
        );

        // 스왑 후 start=12/01 00:00, endExclusive=12/31+1 00:00
        assertEquals(LocalDate.of(2025, 12, 1).atStartOfDay(), fromCaptor.getValue());
        assertEquals(LocalDate.of(2026, 1, 1).atStartOfDay(), toCaptor.getValue());
    }


}
