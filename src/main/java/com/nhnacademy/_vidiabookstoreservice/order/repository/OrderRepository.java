package com.nhnacademy._vidiabookstoreservice.order.repository;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.OrderStatus;
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

    List<Order> findByUserAndCreatedAtBetweenAndDeliveryStatus(
            User user,
            LocalDateTime from,
            LocalDateTime to,
            DeliveryStatus deliveryStatus
    );



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
            @Param("reasonCode") int reasonCode
    );



}
