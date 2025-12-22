package com.nhnacademy._vidiabookstoreservice.order.repository;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.dto.user.UserNetSum;
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

    // 3개월 순수 주문금액 계산
    @Query(value = """
    SELECT
        o.user_id AS userId,
        COALESCE(SUM(
            o.total_book_price
            - o.coupon_discount
            - o.delivery_fee
            - o.packaging_fee
            - COALESCE(pd.cancel_point, 0)
        ), 0) AS netSum
    FROM orders o
    LEFT JOIN (
        SELECT
            order_id,
            SUM(price) AS cancel_point
        FROM point_detail
        WHERE reason = :reasonCode 
        GROUP BY order_id
    ) pd ON o.order_id = pd.order_id
    WHERE o.created_at >= :fromDt
      AND o.created_at < :toDt
    GROUP BY o.user_id
""", nativeQuery = true)
    List<UserNetSum> findUserNetSumLast3Months(
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt,
            @Param("reasonCode") PointReason reasonCode
    );

    /**
     * 구매 확정 -> 순수 주문 금액 계산
     */
    @Query("""
        SELECT
            o.totalBookPrice
            - o.couponDiscount
            - o.deliveryFee
            - o.packagingFee
            - COALESCE(
                (SELECT SUM(pd.price)
                 FROM PointDetail pd
                 WHERE pd.orderId = o.orderId
                   AND pd.reason = :reason), 0
            )
        FROM Order o
        LEFT JOIN PointDetail pd
            ON pd.orderId = o.orderId
            AND pd.reason = :reason
        WHERE o.orderId = :orderId
    """)
    int calculateNetOrderPrice(
            @Param("orderId") Long orderId,
            @Param("reason") PointReason reason
    );




}
