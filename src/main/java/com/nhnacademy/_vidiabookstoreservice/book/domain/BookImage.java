package com.nhnacademy._vidiabookstoreservice.book.domain;

import com.nhnacademy._vidiabookstoreservice.book.domain.converters.ImageTypeConverter;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.ImageType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "image_type", nullable = false)
    @Convert(converter = ImageTypeConverter.class)
    private ImageType imageType;

    @Builder
    public BookImage(Book book, String imageUrl, ImageType imageType) {
        this.book = book;
        this.imageUrl = imageUrl;
        this.imageType = imageType;
    }

    public void setBook(Book book) {
        this.book = book;
        if (!book.getBookImageList().contains(this)) {
            book.getBookImageList().add(this);
        }
    }
}
