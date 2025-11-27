package com.nhnacademy._vidiabookstoreservice.order.domain;

import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "order_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OrderItem {

    @Id
    @Column(name = "order_item_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    @Column(name = "book_id", nullable = false)
    Long bookId; //TODO 도서 가져오기

    @Column(name = "quantity", nullable = false)
    Integer quantity;

    @Column(name = "sale_price", nullable = false)
    Integer salePrice;

    @Column(name = "confirm_status", nullable = false)
    ConfirmStatus confirmStatus = ConfirmStatus.UNCONFIRMED;

    @Builder
    public OrderItem(Order order, long bookId, int quantity, int salePrice, ConfirmStatus confirmStatus) {
        this.order = order;
        this.bookId = bookId;
        this.quantity = quantity;
        this.salePrice = salePrice;
        this.confirmStatus = confirmStatus;
    }
}
