package com.nhnacademy._vidiabookstoreservice.order.repository;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.UserNetSum;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(Long orderId);

    List<Order> findAllByUser_UserId(Long userUserId);

    @Query("select distinct o from Order o " +
            "join fetch o.orderItems oi " +
            "join fetch oi.book " +
            "where o.orderId = :orderId")
    Optional<Order> findByOrderIdWithAll(Long orderId);

    @Query("""
        select o
        from Order o
        where (:deliveryStatus is null or o.deliveryStatus = :deliveryStatus)
          and (
              :keyword is null
              or cast(o.orderId as string) like concat('%', :keyword, '%')
              or lower(o.user.email) like lower(concat('%', :keyword, '%'))
          )
        """)
    Page<Order> searchAdminDeliveries(@Param("deliveryStatus") DeliveryStatus deliveryStatus,
                                      @Param("keyword") String keyword,
                                      Pageable pageable);


    // [수정] 3개월 순수주문금액 산정 (Native Query)
    // 공식: SUM( (확정된 아이템 가격 합계) - (해당 주문의 쿠폰 할인액) )
    @Query(value = """
        SELECT
            T.user_id AS userId,
            SUM(T.order_net_amount) AS netSum
        FROM (
            SELECT
                o.user_id,
                o.order_id,
                -- (확정된 아이템 총액) - (쿠폰 할인액)
                -- GREATEST(0, ...): 혹시 부분 반품 등으로 음수가 나오면 0 처리
                GREATEST(0, SUM(oi.sale_price * oi.quantity) - o.coupon_discount) AS order_net_amount
            FROM orders o
            JOIN order_item oi ON o.order_id = oi.order_id
            WHERE o.created_at >= :fromDt
              AND o.created_at < :toDt
              AND oi.confirm_status = :confirmedStatus -- 구매확정 상태값
            GROUP BY o.order_id, o.user_id, o.coupon_discount
        ) T
        GROUP BY T.user_id
    """, nativeQuery = true)
    List<UserNetSum> findUserNetSumLast3Months(
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt,
            @Param("confirmedStatus") int confirmedStatus // 혹은 String (DB 저장 방식에 따라)
    );

    @Query("""
        select o.totalBookPrice - o.couponDiscount - COALESCE(SUM(pd.price), 0)
        from Order o
        left join PointDetail pd
            on pd.orderId = o.orderId
            and pd.reason= :cancelReason
        where o.orderId = :orderId
        group by o.totalBookPrice, o.couponDiscount
        """)
    int calculateNetOrderPrice(@Param("orderId") Long orderId,
                               @Param("cancelReason")PointReason cancelReason);

}
