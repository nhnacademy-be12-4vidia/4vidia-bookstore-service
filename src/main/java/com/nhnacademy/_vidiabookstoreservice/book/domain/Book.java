package com.nhnacademy._vidiabookstoreservice.book.domain;

import com.nhnacademy._vidiabookstoreservice.book.domain.converters.StockStatusConverter;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.StockStatus;
import com.nhnacademy._vidiabookstoreservice.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "book", indexes = {
    @Index(name = "idx_book_publisher_id", columnList = "publisher_id"),
    @Index(name = "idx_book_category_id", columnList = "category_id")
})
@ToString(exclude = {"bookAuthors", "bookImageList"})
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long id;

    @Column(name = "isbn_13", length = 13)
    
    private String isbn;

    @Column(name = "title", nullable = false, length = 500)
    
    private String title;

    @Column(name = "subtitle", length = 500)
    
    private String subtitle;

    @Column(name = "book_index", columnDefinition = "TEXT")
    
    private String bookIndex;

    @Column(name = "description", columnDefinition = "TEXT")
    
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    
    private Publisher publisher;

    @OneToMany(mappedBy = "book")
    private List<BookAuthor> bookAuthors = new ArrayList<>();

    @Column(name = "published_date")
    
    private LocalDate publishedDate;

    @Column(name = "page_count")
    
    private Integer pageCount;

    @Column(name = "language", length = 10)
    
    private String language;

    @Column(name = "price_standard")
    
    private Integer priceStandard;

    @Column(name = "price_sales")
    
    private Integer priceSales;

    @Column(name = "stock", columnDefinition = "INT DEFAULT 0")
    
    private Integer stock;

    @Column(name = "stock_status")
    
    @Convert(converter = StockStatusConverter.class)
    private StockStatus stockStatus = StockStatus.OUT_OF_STOCK;

    @Column(name = "packaging_available")
    
    private boolean packagingAvailable = false;

    @Column(name = "volume_number", columnDefinition = "INT DEFAULT 1")
    
    private Integer volumeNumber;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC ")
    private List<BookImage> bookImageList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    
    private Category category;

    @Builder
    public Book(String isbn, String title, String description, String subtitle,String bookIndex,
        Publisher publisher,
        LocalDate publishedDate, Integer priceStandard, Integer priceSales, Integer volumeNumber, Category category,
        Integer stock, Integer pageCount, String language, boolean packagingAvailable) {
        this.isbn = isbn;
        this.title = title;
        this.subtitle = subtitle;
        this.bookIndex = bookIndex;
        this.description = description;
        this.publisher = publisher;
        this.category = category;
        this.publishedDate = publishedDate;
        this.priceStandard = priceStandard;
        this.priceSales = priceSales;
        this.volumeNumber = volumeNumber;
        this.stock = stock;
        this.pageCount = pageCount;
        this.language = language;
        this.packagingAvailable = packagingAvailable;
    }

    public void addBookAuthor(BookAuthor bookAuthor) {
        this.bookAuthors.add(bookAuthor);

        if (bookAuthor.getBook() != this) {
            bookAuthor.setBook(this);
        }
    }

    public void addBookImage(BookImage bookImage) {
        this.bookImageList.add(bookImage);

        if (bookImage.getBook() != this) {
            bookImage.setBook(this);
        }
    }
}
