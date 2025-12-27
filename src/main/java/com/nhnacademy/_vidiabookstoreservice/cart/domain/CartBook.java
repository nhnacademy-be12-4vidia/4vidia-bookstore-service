package com.nhnacademy._vidiabookstoreservice.cart.domain;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "cart_book",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cart_book",
                        columnNames = {"cart_id", "book_id"}
                )})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_book_id")
    private Long cartBookId;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Builder
    public CartBook(Cart cart, Book book, Integer quantity){
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }

        this.cart = cart;
        this.book = book;
        this.quantity = quantity;
    }

    public void changeCart(Cart cart) {
        this.cart = cart;
    }


    public void increase(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("증가 수량은 1 이상이어야 합니다.");
        }
        this.quantity += quantity;
    }


    public void changeQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.quantity = quantity;
    }

}