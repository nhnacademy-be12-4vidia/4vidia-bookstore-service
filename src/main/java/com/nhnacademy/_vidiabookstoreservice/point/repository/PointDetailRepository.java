package com.nhnacademy._vidiabookstoreservice.point.repository;


import com.nhnacademy._vidiabookstoreservice.point.domain.PointDetail;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PointDetailRepository extends JpaRepository<PointDetail, Long> {
    /**
     * 사용 가능한 포인트 목록 조회
     * -특정 유저의 적립 포인트 중
     * -price>0 (적립된 포인트)
     * -expiredDate > now (아직 소멸되지 않은)
     * -expiredDate ASC (유효기간이 짧은 것부터 사용하기 위해 오름차순)
     */
    @Query("SELECT p FROM PointDetail p " +
            "WHERE p.userId = :userId " +
            "AND p.remainingPrice > 0 " +
            "AND (p.expiredDate IS NULL OR p.expiredDate >= :now)" +
            "ORDER BY p.expiredDate ASC")
    List<PointDetail> findAvailablePointForUse(@Param("userId")Long userId,
                                               @Param("now") LocalDate now);

    /**
     * 현재 사용 가능한 포인트 총합 조회
     * COALESCE : sum()이 조회 대상이 없을 때 null을 반환 -> null을 0으로 바꿔줘
     */
    @Query("SELECT COALESCE(SUM(p.remainingPrice), 0) FROM PointDetail p " +
            "WHERE p.userId = :userId AND p.remainingPrice > 0 " +
            "AND (p.expiredDate IS NULL OR p.expiredDate >=:now)")
    int getRemainPoint(@Param("userId") Long userId,
                       @Param("now") LocalDate now);

    /**
     * 소멸 대상 포인트 조회 (배치 스케줄러용), 이미 소멸된 포인트 제외
     */
    @Query("SELECT p FROM PointDetail p " +
            "WHERE p.expiredDate < :now AND p.remainingPrice > 0 AND p.reason != :reason")
    List<PointDetail> findExpiredPoints(@Param("now") LocalDate now,
                                        @Param("reason") PointReason reason);

    @Query("""
            SELECT p
            FROM PointDetail p
            WHERE p.userId = :userId
              AND p.expiredDate < :now
              AND p.remainingPrice > 0
              AND p.reason != :reason
        """)
    List<PointDetail> findExpiredPointsByUser(
            @Param("userId") Long userId,
            @Param("now") LocalDate now,
            @Param("reason") PointReason reason
    );

    /**
     *곧 소멸 예정인 포인트 조회
     * now < expiredAt <= limit
     */
    @Query("SELECT COALESCE(SUM(p.remainingPrice), 0) FROM PointDetail p " +
            "WHERE p.userId = :userId " +
            "AND p.remainingPrice > 0 " +
            "AND p.expiredDate > :now " +
            "AND p.expiredDate <= :limit")
    int getExpiringSoon(@Param("userId") Long userId,
                        @Param("now") LocalDate now,
                        @Param("limit") LocalDate limit);

    /**
     * 포인트 내역 최신 순 조회
     */
    Page<PointDetail> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    /**
     * 포인트 내역 중 '적립' 내역만 (price > 0)
     */
    Page<PointDetail> findByUserIdAndPriceGreaterThanOrderByCreatedAtDesc(Long userId, int price, Pageable pageable);

    /**
     * 포인트 내역 중 '사용' 내역만 (price < 0)
     */
    Page<PointDetail> findByUserIdAndPriceLessThanOrderByCreatedAtDesc(Long userId, int price, Pageable pageable);

    /**
     * 중복 환불 방지 체크
     * 동일 orderId + 동일 Reason 로 이미 환불 이력이 있으면 true
     */
    boolean existsByUserIdAndOrderIdAndReason(Long userId, Long orderId, PointReason reason);

    /**
     * 주문 시 기록된 포인트 내역 조회
     */
    Optional<PointDetail> findByOrderIdAndReason(Long orderId, PointReason reason);

    /**
     * 포인트 환불 가능한 내역 (만료일 전, remainingPrice < price), 만료일이 많이 남은 것부터
     */
    @Query("""
        select p
        from PointDetail p
        where p.userId = :userId
          and p.expiredDate >= :now
          and p.remainingPrice < p.price
        order by p.expiredDate desc
    """)
    List<PointDetail> findPointForRefund(@Param("userId") Long userId,
                                         @Param("now") LocalDate now);
}
