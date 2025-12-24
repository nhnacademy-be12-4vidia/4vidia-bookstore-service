package com.nhnacademy._vidiabookstoreservice.point.repository;

import com.nhnacademy._vidiabookstoreservice.book.config.QueryDslConfig;
import com.nhnacademy._vidiabookstoreservice.point.domain.PointDetail;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class PointDetailRepositoryTest {

    @Autowired
    PointDetailRepository pointDetailRepository;

    @Autowired
    EntityManager em;

    private final LocalDate NOW = LocalDate.of(2025, 12, 24);
    private final LocalDateTime NOW_DT = LocalDateTime.of(2025, 12, 24, 10, 0);

    private PointDetail pd(Long userId, Long orderId, int price, LocalDateTime createdAt,
                           LocalDate expiredDate, PointReason reason, Integer remainingPrice) {
        return PointDetail.builder()
                .userId(userId)
                .orderId(orderId)
                .price(price)
                .createdAt(createdAt)
                .expiredDate(expiredDate)
                .reason(reason)
                .remainingPrice(remainingPrice != null ? remainingPrice : 0)
                .build();
    }

    private void persist(PointDetail p) {
        em.persist(p);
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("사용 가능한 포인트 목록 조회: 정렬 및 조건 확인")
    void findAvailablePointForUse_success() {
        // given
        // 1. 10일 뒤 만료
        persist(pd(1L, null, 1000, NOW_DT, NOW.plusDays(10), PointReason.ORDER_REWARD, 1000));
        // 2. 3일 뒤 만료
        persist(pd(1L, null, 2000, NOW_DT, NOW.plusDays(3), PointReason.POLICY_REWARD, 1500));
        // 3. 무기한 포인트 (null) -> H2에서는 정렬 시 보통 가장 먼저 나옴
        persist(pd(1L, null, 300,  NOW_DT, null, PointReason.ORDER_REWARD, 300));
        // 4. 어제 만료 (제외 대상)
        persist(pd(1L, null, 500,  NOW_DT, NOW.minusDays(1), PointReason.ORDER_REWARD, 500));

        // when
        List<PointDetail> result = pointDetailRepository.findAvailablePointForUse(1L, NOW);

        // then
        assertThat(result).hasSize(3); // 만료된 4번 제외하고 3개

        // 정렬 검증 (null 제외하고 날짜 있는 것들끼리 비교)
        List<LocalDate> dates = result.stream()
                .map(PointDetail::getExpiredDate)
                .filter(java.util.Objects::nonNull) // null(무기한) 제외
                .toList();

        // 날짜가 있는 데이터들끼리는 오름차순(ASC)이어야 함
        assertThat(dates).isSorted();

        // 구체적인 날짜 확인 (리스트에 해당 날짜들이 포함되어 있는지 확인)
        assertThat(result).extracting("expiredDate")
                .contains(NOW.plusDays(3), NOW.plusDays(10));
    }

    @Test
    @DisplayName("잔여 포인트 합계 조회")
    void getRemainPoint_success() {
        // given
        persist(pd(1L, null, 1000, NOW_DT, NOW.plusDays(5), PointReason.ORDER_REWARD, 700));
        persist(pd(1L, null, 500,  NOW_DT, NOW.plusDays(5), PointReason.ORDER_REWARD, 300));
        persist(pd(1L, null, 500,  NOW_DT, NOW.minusDays(1), PointReason.ORDER_REWARD, 500)); // 만료 제외

        // when
        int remainPoint = pointDetailRepository.getRemainPoint(1L, NOW);

        // then
        assertThat(remainPoint).isEqualTo(1000);
    }

    @Test
    @DisplayName("소멸 대상 포인트 조회 (배치용)")
    void findExpiredPoints_success() {
        // given
        persist(pd(1L, null, 1000, NOW_DT, NOW.minusDays(1), PointReason.ORDER_REWARD, 100));
        persist(pd(1L, null, 1000, NOW_DT, NOW.minusDays(1), PointReason.POINT_EXPIRE, 100)); // 이미 소멸처리된 건 제외

        // when
        List<PointDetail> expired = pointDetailRepository.findExpiredPoints(NOW, PointReason.POINT_EXPIRE);

        // then
        assertThat(expired).hasSize(1);
        assertThat(expired.get(0).getReason()).isNotEqualTo(PointReason.POINT_EXPIRE);
    }

    @Test
    @DisplayName("곧 소멸 예정인 포인트 합계 조회")
    void getExpiringSoon_success() {
        // given
        persist(pd(1L, null, 1000, NOW_DT, NOW.plusDays(3), PointReason.ORDER_REWARD, 100)); // 범위 내
        persist(pd(1L, null, 1000, NOW_DT, NOW.plusDays(7), PointReason.ORDER_REWARD, 200)); // 범위 내
        persist(pd(1L, null, 1000, NOW_DT, NOW.plusDays(10), PointReason.ORDER_REWARD, 500)); // 범위 밖

        // when
        int sum = pointDetailRepository.getExpiringSoon(1L, NOW, NOW.plusDays(7));

        // then
        assertThat(sum).isEqualTo(300);
    }

    @Test
    @DisplayName("날짜 기간 내 포인트 내역 페이징 조회")
    void findByUserIdAndCreatedAtBetween_success() {
        // given
        persist(pd(1L, null, 1000, NOW_DT.minusDays(2), null, PointReason.ORDER_REWARD, 1000));
        persist(pd(1L, null, 500,  NOW_DT, null, PointReason.ORDER_REWARD, 500));

        // when
        Page<PointDetail> page = pointDetailRepository.findByUserIdAndCreatedAtBetween(
                1L, NOW_DT.minusDays(1), NOW_DT.plusDays(1), PageRequest.of(0, 10)
        );

        // then
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("기간 내 적립 포인트(Price > 0) 페이징 조회")
    void findByUserIdAndCreatedAtBetweenAndPriceGreaterThan_success() {
        // given
        persist(pd(1L, null, 1000, NOW_DT, null, PointReason.ORDER_REWARD, 1000));
        persist(pd(1L, 100L, -500, NOW_DT, null, PointReason.ORDER_USE, 0));

        // when
        Page<PointDetail> page = pointDetailRepository.findByUserIdAndCreatedAtBetweenAndPriceGreaterThan(
                1L, NOW_DT.minusDays(1), NOW_DT.plusDays(1), 0, PageRequest.of(0, 10)
        );

        // then
        assertThat(page.getContent()).allMatch(p -> p.getPrice() > 0);
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("환불 가능 내역 조회: 사용된 포인트가 있고 만료 전인 것 (유효기간 긴 순서)")
    void findPointForRefund_success() {
        // given
        persist(pd(1L, null, 1000, NOW_DT, NOW.plusDays(1), PointReason.ORDER_REWARD, 200)); // 포함 (일부 사용됨)
        persist(pd(1L, null, 1000, NOW_DT, NOW.plusDays(10), PointReason.ORDER_REWARD, 500)); // 포함 (일부 사용됨)
        persist(pd(1L, null, 1000, NOW_DT, NOW.plusDays(5), PointReason.ORDER_REWARD, 1000)); // 제외 (사용 안 함)

        // when
        List<PointDetail> result = pointDetailRepository.findPointForRefund(1L, NOW);

        // then
        assertThat(result).hasSize(2);
        // order by expiredDate desc 확인
        assertThat(result.get(0).getExpiredDate()).isEqualTo(NOW.plusDays(10));
    }

    @Test
    @DisplayName("특정 주문과 사유에 대해 이미 처리된 포인트 차액(환불된 양 등) 합계")
    void sumRefundedPoint_success() {
        // given
        Long orderId = 999L;
        persist(pd(1L, orderId, 1000, NOW_DT, null, PointReason.ORDER_REWARD, 700)); // 차액 300
        persist(pd(1L, orderId, 500,  NOW_DT, null, PointReason.ORDER_REWARD, 400)); // 차액 100

        // when
        int sum = pointDetailRepository.sumRefundedPoint(orderId, PointReason.ORDER_REWARD);

        // then
        assertThat(sum).isEqualTo(400); // (1000-700) + (500-400)
    }
}