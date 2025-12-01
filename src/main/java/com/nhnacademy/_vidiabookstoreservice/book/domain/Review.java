package com.nhnacademy._vidiabookstoreservice.book.domain;

import com.nhnacademy._vidiabookstoreservice.global.entity.BaseEntity;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name = "review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    private OrderItem orderItem;

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

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC ")
    private List<ReviewImage> imageList = new ArrayList<>();

    public void updateContent(String content, Integer rating) {
        this.content = content;
        this.rating = rating;
    }

    public void addReviewImage(ReviewImage reviewImage) {
        this.imageList.add(reviewImage);

        if (reviewImage.getReview() != this) {
            reviewImage.setReview(this);
        }
    }

    public void updateHasPhotoStatus() {
        this.hasPhoto = !this.imageList.isEmpty();
    }

    @Builder
    public Review(User user, OrderItem orderItem, Book book, Integer rating, String content,
        boolean hasPhoto) {
        this.user = user;
        this.orderItem = orderItem;
        this.book = book;
        this.rating = rating;
        this.content = content;
        this.hasPhoto = hasPhoto;
    }
}
