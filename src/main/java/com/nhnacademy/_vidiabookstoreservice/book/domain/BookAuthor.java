package com.nhnacademy._vidiabookstoreservice.book.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookAuthor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_author_id")
    Long id;

    @ManyToOne
    @JoinColumn(name = "book_id")
    @Setter
    Book book;

    @ManyToOne
    @JoinColumn(name = "author_id")
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

}
