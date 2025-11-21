package com.nhnacademy._vidiabookstoreservice.book.domain;

import com.nhnacademy._vidiabookstoreservice.book.domain.enums.StockStatus;
import com.nhnacademy._vidiabookstoreservice.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long id;

    @Column(name = "isbn_13", length = 13)
    @Setter
    private String isbn;

    @Column(name = "title", nullable = false, length = 1000)
    @Setter
    private String title;

    @Column(name = "subtitle")
    @Setter
    private String subtitle;

    @Column(name = "index", columnDefinition = "TEXT")
    @Setter
    private String index;

    @Column(name = "description", columnDefinition = "TEXT")
    @Setter
    private String description;

    @ManyToOne
    @Setter
    private Publisher publisher;

    @Column(name = "published_date")
    @Setter
    private LocalDate publishedDate;

    @Column(name = "paged_count")
    @Setter
    private Integer pageCount;

    @Column(name = "language", length = 10)
    @Setter
    private String language;

    @Column(name = "price_standard")
    @Setter
    private Integer priceStandard;

    @Column(name = "price_sales")
    @Setter
    private Integer priceSales;

    @Column(name = "stock", columnDefinition = "INT DEFAULT 0")
    @Setter
    private Integer stock;

    @Column(name = "stock_status")
    @Setter
    @Enumerated(value = EnumType.STRING)
    private StockStatus stockStatus = StockStatus.OUT_OF_STOCK;

    @Column(name = "packaging_available")
    @Setter
    private boolean packagingAvailable = false;

    @Column(name = "volume_number", columnDefinition = "INT DEFAULT 0")
    @Setter
    private Integer volumeNumber;

    @Builder
    public Book(String isbn, String title, String description,
        Publisher publisher,
        LocalDate publishedDate, Integer priceStandard, Integer priceSales) {
        this.isbn = isbn;
        this.title = title;
        this.description = description;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.priceSales = priceSales;
    }




}
