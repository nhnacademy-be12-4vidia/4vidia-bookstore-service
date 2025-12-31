package com.nhnacademy._vidiabookstoreservice.user.domain;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "likes", // 'like' = sql 예약어
        uniqueConstraints = @UniqueConstraint(name = "uk_user_book", columnNames = {"user_id", "book_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id", nullable = false)
    private Long likeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;


    @Builder
    public Like(User user, Book book) {
        this.user = user;
        this.book = book;
    }
}
