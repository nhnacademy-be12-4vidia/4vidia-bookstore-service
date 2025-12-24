package com.nhnacademy._vidiabookstoreservice.order.domain;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.refund.domain.RefundItem;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundItemStatus;
import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    Book book;

    @Column(name = "quantity", nullable = false)
    Integer quantity;

    @Column(name = "sale_price", nullable = false)
    Integer salePrice;

    @Column(name = "confirm_status", nullable = false)
    ConfirmStatus confirmStatus = ConfirmStatus.UNCONFIRMED;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Packaging> packagings = new ArrayList<>();

    @OneToMany(mappedBy = "orderItem")
    private List<RefundItem> refundItems = new ArrayList<>();

    @Builder
    public OrderItem(Order order, Book book, int quantity, int salePrice, ConfirmStatus confirmStatus) {
        this.order = order;
        this.book = book;
        this.quantity = quantity;
        this.salePrice = salePrice;
        this.confirmStatus = confirmStatus;
    }

    /**
     * 반품 가능 여부 판단
     * 조건: 구매 미확정이거나, 가장 최근 반품 신청이 거절된 경우
     */
    public boolean isReturnable() {
        if (this.confirmStatus == ConfirmStatus.CONFIRMED) {
            return false;
        }

        if (this.refundItems == null || this.refundItems.isEmpty()) {
            return true;
        }

        return this.refundItems.stream()
                .max(Comparator.comparing(RefundItem::getRefundItemId)) // 제일 최근 등록된 반품
                .map(ri -> ri.getRefundItemStatus() == RefundItemStatus.REJECTED)
                .orElse(false);
    }
}
