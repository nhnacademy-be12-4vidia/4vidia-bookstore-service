package com.nhnacademy._vidiabookstoreservice.book.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Setter
    Author author;

    @Column(name = "author_role")
    @Setter
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
