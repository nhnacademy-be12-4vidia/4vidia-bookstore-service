package com.nhnacademy._vidiabookstoreservice.point.repository;

import com.nhnacademy._vidiabookstoreservice.point.domain.PointDetail;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PointDetailRepositoryTest {

    @Autowired
    PointDetailRepository pointDetailRepository;

    @Autowired
    EntityManager em;

    private final LocalDate NOW = LocalDate.of(2025, 12, 23);
    private final LocalDateTime NOW_DT = LocalDateTime.of(2025, 12, 23, 10, 0);

    private PointDetail pd(
            Long userId,
            Long orderId,
            int price,
            LocalDateTime createdAt,
            LocalDate expiredDate,
            PointReason reason,
            Integer remainingPrice
    ) {
        return PointDetail.builder()
                .userId(userId)
                .orderId(orderId)
                .price(price)
                .createdAt(createdAt)
                .expiredDate(expiredDate)
                .reason(reason)
                .remainingPrice(remainingPrice) // null이면 builder 내부에서 0 처리
                .build();
    }

    private void persist(PointDetail p) {
        em.persist(p);
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("findAvailablePointForUse: remainingPrice>0 AND (expiredDate null or >= now) AND expiredDate ASC")
    void findAvailablePointForUse_success() {
        // given
        persist(pd(1L, null, 1000, NOW_DT.minusDays(3), NOW.plusDays(10), PointReason.ORDER_REWARD, 1000));
        persist(pd(1L, null, 2000, NOW_DT.minusDays(2), NOW.plusDays(3), PointReason.POLICY_REWARD, 1500));
        persist(pd(1L, null, 500,  NOW_DT.minusDays(1), NOW.plusDays(2), PointReason.ORDER_REWARD, 0));        // 제외(remaining=0)
        persist(pd(1L, null, 700,  NOW_DT.minusDays(1), NOW.minusDays(1), PointReason.ORDER_REWARD, 700));     // 제외(만료)
        persist(pd(2L, null, 999,  NOW_DT.minusDays(1), NOW.plusDays(1), PointReason.ORDER_REWARD, 999));     // 제외(다른 유저)
        persist(pd(1L, null, 300,  NOW_DT.minusDays(1), null,            PointReason.ORDER_REWARD, 300));     // 포함(expired null)

        // when
        List<PointDetail> result = pointDetailRepository.findAvailablePointForUse(1L, NOW);

        // then
        assertThat(result).allMatch(p -> p.getUserId().equals(1L));
        assertThat(result).allMatch(p -> p.getRemainingPrice() > 0);
        assertThat(result).allSatisfy(p -> {
            if (p.getExpiredDate() != null) {
                assertThat(p.getExpiredDate()).isAfterOrEqualTo(NOW);
            }
        });

        // expiredDate ASC 정렬 확인(단, null은 DB 정렬 규칙이 달라질 수 있어 null 제외하고 검사)
        List<LocalDate> nonNullDates = result.stream()
                .map(PointDetail::getExpiredDate)
                .filter(d -> d != null)
                .toList();
        assertThat(nonNullDates).isSorted();
    }

    @Test
    @DisplayName("getRemainPoint: remainingPrice 합계(없으면 0)")
    void getRemainPoint_success_and_empty() {
        // given
        persist(pd(1L, null, 1000, NOW_DT, NOW.plusDays(1), PointReason.ORDER_REWARD, 500));
        persist(pd(1L, null, 2000, NOW_DT, NOW.plusDays(2), PointReason.POLICY_REWARD, 1000));
        persist(pd(1L, null, 999,  NOW_DT, NOW.plusDays(3), PointReason.ORDER_REWARD, 0));        // 제외
        persist(pd(1L, null, 111,  NOW_DT, NOW.minusDays(1), PointReason.ORDER_REWARD, 111));     // 제외(만료)
        persist(pd(2L, null, 777,  NOW_DT, NOW.plusDays(10), PointReason.ORDER_REWARD, 777));     // 제외(다른 유저)

        // when
        int sum = pointDetailRepository.getRemainPoint(1L, NOW);
        int empty = pointDetailRepository.getRemainPoint(999L, NOW);

        // then
        assertThat(sum).isEqualTo(1500);
        assertThat(empty).isEqualTo(0);
    }

    @Test
    @DisplayName("findExpiredPoints: expiredDate < now AND remainingPrice>0 AND reason != 제외사유")
    void findExpiredPoints_success() {
        // given
        persist(pd(1L, null, 1000, NOW_DT, NOW.minusDays(1), PointReason.ORDER_REWARD, 100));     // 포함
        persist(pd(1L, null, 1000, NOW_DT, NOW.minusDays(2), PointReason.POINT_EXPIRE, 100));    // 제외(reason)
        persist(pd(1L, null, 1000, NOW_DT, NOW.minusDays(1), PointReason.ORDER_REWARD, 0));      // 제외(remaining=0)
        persist(pd(1L, null, 1000, NOW_DT, NOW.plusDays(1),  PointReason.ORDER_REWARD, 100));    // 제외(만료 아님)

        // when
        List<PointDetail> result = pointDetailRepository.findExpiredPoints(NOW, PointReason.POINT_EXPIRE);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExpiredDate()).isBefore(NOW);
        assertThat(result.get(0).getRemainingPrice()).isGreaterThan(0);
        assertThat(result.get(0).getReason()).isNotEqualTo(PointReason.POINT_EXPIRE);
    }

    @Test
    @DisplayName("findExpiredPointsByUser: 특정 유저의 만료 포인트만 조회")
    void findExpiredPointsByUser_success() {
        // given
        persist(pd(1L, null, 1000, NOW_DT, NOW.minusDays(1), PointReason.ORDER_REWARD, 100)); // 포함
        persist(pd(2L, null, 1000, NOW_DT, NOW.minusDays(1), PointReason.ORDER_REWARD, 100)); // 제외

        // when
        List<PointDetail> result = pointDetailRepository.findExpiredPointsByUser(1L, NOW, PointReason.POINT_EXPIRE);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getExpiringSoon: now < expiredDate <= limit 인 remainingPrice 합계")
    void getExpiringSoon_success() {
        // given
        persist(pd(1L, null, 1000, NOW_DT, NOW.plusDays(1), PointReason.ORDER_REWARD, 100)); // 포함
        persist(pd(1L, null, 1000, NOW_DT, NOW.plusDays(7), PointReason.ORDER_REWARD, 200)); // 포함
        persist(pd(1L, null, 1000, NOW_DT, NOW.plusDays(8), PointReason.ORDER_REWARD, 300)); // 제외(limit 초과)
        persist(pd(1L, null, 1000, NOW_DT, NOW,            PointReason.ORDER_REWARD, 400)); // 제외(expiredDate > now 조건)

        // when
        int sum = pointDetailRepository.getExpiringSoon(1L, NOW, NOW.plusDays(7));

        // then
        assertThat(sum).isEqualTo(300);
    }

    @Test
    @DisplayName("findByUserIdOrderByCreatedAtDesc: 최신순 페이징 조회")
    void findByUserIdOrderByCreatedAtDesc_success() {
        // given (createdAt을 일부러 다르게)
        persist(pd(1L, null, 100, NOW_DT.minusMinutes(10), NOW.plusDays(1), PointReason.ORDER_REWARD, 100));
        persist(pd(1L, null, 200, NOW_DT.minusMinutes(5),  NOW.plusDays(1), PointReason.ORDER_REWARD, 200));
        persist(pd(1L, 10L, -50, NOW_DT.minusMinutes(1),  null,           PointReason.ORDER_USE, null));

        // when
        Page<PointDetail> page = pointDetailRepository.findByUserIdOrderByCreatedAtDesc(
                1L, PageRequest.of(0, 2)
        );

        // then
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).getCreatedAt())
                .isAfterOrEqualTo(page.getContent().get(1).getCreatedAt());
    }

    @Test
    @DisplayName("findByUserIdAndPriceGreaterThanOrderByCreatedAtDesc: 적립(price > 0)만")
    void findByUserIdAndPriceGreaterThan_success() {
        // given
        persist(pd(1L, null, 100, NOW_DT, NOW.plusDays(1), PointReason.ORDER_REWARD, 100));
        persist(pd(1L, 10L, -50, NOW_DT.plusSeconds(1), null, PointReason.ORDER_USE, null));

        // when
        Page<PointDetail> page = pointDetailRepository.findByUserIdAndPriceGreaterThanOrderByCreatedAtDesc(
                1L, 0, PageRequest.of(0, 10)
        );

        // then
        assertThat(page.getContent()).allMatch(p -> p.getPrice() > 0);
    }

    @Test
    @DisplayName("findByUserIdAndPriceLessThanOrderByCreatedAtDesc: 사용(price < 0)만")
    void findByUserIdAndPriceLessThan_success() {
        // given
        persist(pd(1L, null, 100, NOW_DT, NOW.plusDays(1), PointReason.ORDER_REWARD, 100));
        persist(pd(1L, 10L, -50, NOW_DT.plusSeconds(1), null, PointReason.ORDER_USE, null));

        // when
        Page<PointDetail> page = pointDetailRepository.findByUserIdAndPriceLessThanOrderByCreatedAtDesc(
                1L, 0, PageRequest.of(0, 10)
        );

        // then
        assertThat(page.getContent()).allMatch(p -> p.getPrice() < 0);
    }

    @Test
    @DisplayName("existsByUserIdAndOrderIdAndReason: 중복 환불 방지 체크")
    void existsByUserIdAndOrderIdAndReason_success() {
        // given
        persist(pd(1L, 100L, 300, NOW_DT, null, PointReason.ORDER_CANCEL_REFUND, 0));

        // when
        boolean exists = pointDetailRepository.existsByUserIdAndOrderIdAndReason(1L, 100L, PointReason.ORDER_CANCEL_REFUND);
        boolean notExists = pointDetailRepository.existsByUserIdAndOrderIdAndReason(1L, 999L, PointReason.ORDER_CANCEL_REFUND);

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("findByOrderIdAndReason: 주문 포인트 내역 단건 조회")
    void findByOrderIdAndReason_success() {
        // given
        persist(pd(1L, 777L, -300, NOW_DT, null, PointReason.ORDER_USE, null));

        // when
        var opt = pointDetailRepository.findByOrderIdAndReason(777L, PointReason.ORDER_USE);

        // then
        assertThat(opt).isPresent();
        assertThat(opt.get().getOrderId()).isEqualTo(777L);
        assertThat(opt.get().getReason()).isEqualTo(PointReason.ORDER_USE);
    }

    @Test
    @DisplayName("findPointForRefund: expiredDate>=now AND remainingPrice < price AND expiredDate desc")
    void findPointForRefund_success() {
        // given
        persist(pd(1L, 10L, 1000, NOW_DT, NOW.plusDays(5),  PointReason.ORDER_REWARD, 200)); // 포함
        persist(pd(1L, 11L, 500,  NOW_DT, NOW.plusDays(10), PointReason.ORDER_REWARD, 500)); // 제외(remaining==price)
        persist(pd(1L, 12L, 1000, NOW_DT, NOW.minusDays(1), PointReason.ORDER_REWARD, 100)); // 제외(만료)
        persist(pd(1L, 13L, 1000, NOW_DT, NOW.plusDays(20), PointReason.ORDER_REWARD, 300)); // 포함(더 늦게 만료)

        // when
        List<PointDetail> result = pointDetailRepository.findPointForRefund(1L, NOW);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(p -> p.getExpiredDate().isAfter(NOW)|| p.getExpiredDate().isEqual(NOW));
        assertThat(result).allMatch(p -> p.getRemainingPrice() < p.getPrice());

        // expiredDate desc 확인
        assertThat(result.get(0).getExpiredDate()).isAfter(result.get(1).getExpiredDate());
    }

    @Test
    @DisplayName("sumRefundedPoint: (price - remainingPrice) 합계")
    void sumRefundedPoint_success() {
        // given
        // (1000-700)=300, (500-200)=300 => 600
        persist(pd(1L, 900L, 1000, NOW_DT, NOW.plusDays(10), PointReason.ORDER_REWARD, 700));
        persist(pd(1L, 900L, 500,  NOW_DT, NOW.plusDays(10), PointReason.ORDER_REWARD, 200));
        persist(pd(1L, 900L, 999,  NOW_DT, NOW.plusDays(10), PointReason.ORDER_CANCEL_REFUND, 0)); // reason 달라서 제외

        // when
        int sum = pointDetailRepository.sumRefundedPoint(900L, PointReason.ORDER_REWARD);

        // then
        assertThat(sum).isEqualTo(600);
    }
}
