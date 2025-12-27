package com.nhnacademy._vidiabookstoreservice.cart.domain;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "cart")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    @Column(name = "user_id")
    private Long userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartBook> cartBooks = new ArrayList<>();

    public Cart(Long userId) {
        this.userId = userId;
    }

    public void addItem(Book book, Integer quantity) {
        CartBook existed = cartBooks.stream()
                .filter(cb -> cb.getBook().getId().equals(book.getId()))
                .findFirst()
                .orElse(null);

        if (existed != null) {
            existed.increase(quantity);
        } else {
            CartBook cartBook = CartBook.builder()
                    .book(book)
                    .quantity(quantity)
                    .build();
            cartBook.changeCart(this);
            cartBooks.add(cartBook);
        }
    }

}