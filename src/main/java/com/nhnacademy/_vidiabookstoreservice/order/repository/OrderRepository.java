package com.nhnacademy._vidiabookstoreservice.order.repository;

import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderCountResponse;
import com.nhnacademy._vidiabookstoreservice.order.service.impl.OrderServiceImpl;
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

    // 전체 조회
    Page<Order> findAllByUser_UserId(Long userUserId, Pageable pageable);
    // 배송 상태별 조회
    Page<Order> findAllByUser_UserIdAndDeliveryStatus(Long userUserId, DeliveryStatus deliveryStatus, Pageable pageable);
    // 반품/교환 요청 조회 (OrderItem 상태가 REFUND_REQUESTED인 주문들)
    @Query("SELECT DISTINCT ri.orderItem.order FROM RefundItem ri " +
            "WHERE ri.orderItem.order.user.userId = :userId " +
            "AND ri.refundItemStatus = com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundItemStatus.PROCESS")
    Page<Order> findRefundRequestsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT new com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderCountResponse(
            COUNT(DISTINCT o),
            SUM(CASE WHEN o.deliveryStatus = com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus.WAITING THEN 1 ELSE 0 END),
            SUM(CASE WHEN o.deliveryStatus = com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus.SHIPPING THEN 1 ELSE 0 END),
            SUM(CASE WHEN o.deliveryStatus = com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus.DELIVERED THEN 1 ELSE 0 END),
            SUM(CASE WHEN o.deliveryStatus = com.nhnacademy._vidiabookstoreservice.order.domain.enums.DeliveryStatus.CANCELED THEN 1 ELSE 0 END)
        )
        FROM Order o
        WHERE o.user.userId = :userId
    """)
    OrderCountResponse countOrdersByUserId(@Param("userId") Long userId);
    // TODO 수정할 예정.......
//    @Query("""
//            SELECT new com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderCountResponse(
//                COUNT(DISTINCT o),
//                SUM(CASE WHEN o.deliveryStatus = :waiting THEN 1 ELSE 0 END),
//                SUM(CASE WHEN o.deliveryStatus = :shipping THEN 1 ELSE 0 END),
//                SUM(CASE WHEN o.deliveryStatus = :delivered THEN 1 ELSE 0 END),
//                SUM(CASE WHEN o.deliveryStatus = :canceled THEN 1 ELSE 0 END)
//            )
//            FROM Order o
//            WHERE o.user.userId = :userId
//        """)
//    OrderCountResponse countOrdersByUserId(
//            @Param("userId") Long userId,
//            @Param("waiting") DeliveryStatus waiting,
//            @Param("shipping") DeliveryStatus shipping,
//            @Param("delivered") DeliveryStatus delivered,
//            @Param("canceled") DeliveryStatus canceled
//    );


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
                - COALESCE(
                    (SELECT SUM(pd.price)
                     FROM PointDetail pd
                     WHERE pd.orderId = o.orderId
                       AND pd.reason = :reason), 0)
            FROM Order o
            WHERE o.orderId = :orderId
        """)
    int calculateNetOrderPrice(
            @Param("orderId") Long orderId,
            @Param("reason") PointReason reason
    );




}
