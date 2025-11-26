package com.nhnacademy._vidiabookstoreservice.book.domain;

import com.nhnacademy._vidiabookstoreservice.global.entity.BaseEntity;
import jakarta.persistence.Column;
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
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "review_content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "has_photo", nullable = false)
    @Setter
    private boolean hasPhoto;

    public void updateContent(String content, Integer rating) {
        this.content = content;
        this.rating = rating;
    }

    @Builder
    public Review(Long userId, Long orderItemId, Book book, Integer rating, String content,
        boolean hasPhoto) {
        this.userId = userId;
        this.orderItemId = orderItemId;
        this.book = book;
        this.rating = rating;
        this.content = content;
        this.hasPhoto = hasPhoto;
    }
}
