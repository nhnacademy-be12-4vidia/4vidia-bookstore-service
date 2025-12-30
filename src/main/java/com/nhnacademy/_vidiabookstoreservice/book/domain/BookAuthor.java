package com.nhnacademy._vidiabookstoreservice.book.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "book_author", indexes = {
    @Index(name = "idx_book_id", columnList = "book_id"),
    @Index(name = "idx_author_id", columnList = "author_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_book_author", columnNames = {"book_id", "author_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookAuthor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_author_id")
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    Author author;

    @Column(name = "author_role")
    String role;

    @Builder
    public BookAuthor(Book book, Author author, String role) {
        this.book = book;
        this.author = author;
        this.role = role;
    }

    public void setBook(Book book) {
        this.book = book;
        if (!book.getBookAuthorList().contains(this)) {
            book.getBookAuthorList().add(this);
        }
    }

}
