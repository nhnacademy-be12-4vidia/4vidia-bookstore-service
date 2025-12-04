package com.nhnacademy._vidiabookstoreservice.order.domain;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
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

    @Builder
    public OrderItem(Order order, Book book, int quantity, int salePrice, ConfirmStatus confirmStatus) {
        this.order = order;
        this.book = book;
        this.quantity = quantity;
        this.salePrice = salePrice;
        this.confirmStatus = confirmStatus;
    }
}
