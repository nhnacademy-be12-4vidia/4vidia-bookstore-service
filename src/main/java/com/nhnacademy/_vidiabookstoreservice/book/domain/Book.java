package com.nhnacademy._vidiabookstoreservice.book.domain;

import com.nhnacademy._vidiabookstoreservice.book.domain.converters.StockStatusConverter;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.StockStatus;
import com.nhnacademy._vidiabookstoreservice.book.exception.invalid.BookStockNotEnoughException;
import com.nhnacademy._vidiabookstoreservice.book.service.impl.BookServiceImpl.AuthorSyncData;
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
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    private List<BookAuthor> bookAuthorList = new ArrayList<>();

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

    @Column(name = "stock", columnDefinition = "INT DEFAULT 10")
    private Integer stock;

    @Column(name = "stock_status", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    @Convert(converter = StockStatusConverter.class)
    private StockStatus stockStatus = StockStatus.IN_STOCK;

    @Column(name = "packaging_available", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean packagingAvailable = true;

    @Column(name = "volume_number", columnDefinition = "INT DEFAULT 1")
    private Integer volumeNumber;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC ")
    private List<BookImage> bookImageList = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookTag> bookTagList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    
    private Category category;

    @Builder
    public Book(String isbn, String title, String description, String subtitle,String bookIndex,
        Publisher publisher,
        LocalDate publishedDate, Integer priceStandard, Integer priceSales, Integer volumeNumber, Category category,
        Integer stock, StockStatus stockStatus, Integer pageCount, String language, boolean packagingAvailable) {
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
        this.stockStatus = stockStatus;
        this.pageCount = pageCount;
        this.language = language;
        this.packagingAvailable = packagingAvailable;
    }

    public void addBookAuthor(BookAuthor bookAuthor) {
        this.bookAuthorList.add(bookAuthor);

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

    public void addBookTag(BookTag bookTag) {
        this.bookTagList.add(bookTag);

        if (bookTag.getBook() != this) {
            bookTag.setBook(this);
        }
    }

    public void addTag(Tag tag) {
        BookTag bookTag = BookTag.builder()
            .tag(tag)
            .book(this)
            .build();
        this.addBookTag(bookTag);
    }

    public void decreaseStock(int quantity) {
        int restStock = this.stock - quantity;

        if (restStock < 0) {
            throw new BookStockNotEnoughException();
        }
        this.stock = restStock;

        if (this.stock == 0) {
            this.stockStatus = StockStatus.OUT_OF_STOCK;
        }
    }

    public void increaseStock(int quantity) {
        this.stock += quantity;
    }

    public void updateBasicInfo(String title, String subtitle, String description, String bookIndex,
        LocalDate publishedDate, Publisher publisher, Category category, String language,
        Integer pageCount, Integer volumeNumber) {

        this.title = title;
        this.subtitle = subtitle;
        this.description = description;
        this.bookIndex = bookIndex;
        this.publishedDate = publishedDate;
        this.publisher = publisher;
        this.category = category;
        this.language = language;
        this.pageCount = pageCount;
        this.volumeNumber = volumeNumber;

    }

    public void updatePriceAndStock(Integer priceStandard, Integer priceSales, Integer stock,
        boolean packagingAvailable, StockStatus stockStatus) {

        this.priceStandard = priceStandard;
        this.priceSales = priceSales;
        this.stock = stock;
        this.packagingAvailable = packagingAvailable;
        this.stockStatus = stockStatus;
    }

    public void updateBookAuthors(List<BookAuthor> newBookAuthors) {
        this.bookAuthorList.clear();
        for (BookAuthor newAuthor : newBookAuthors) {
            this.addBookAuthor(newAuthor);
        }
    }

    public void updateBookTag(List<BookTag> newBookTags) {
        this.bookTagList.clear();
        for (BookTag newTag : newBookTags) {
            this.addBookTag(newTag);
        }
    }

    public void updateBookImage(List<BookImage> newBookImages) {
        this.bookImageList.clear();
        for (BookImage newImage : newBookImages) {
            this.addBookImage(newImage);
        }
    }

    public void syncBookTags(List<Tag> newTags) {
        Set<String> newTagNames = newTags.stream().map(Tag::getName).collect(Collectors.toSet());

        List<BookTag> toRemove = this.bookTagList.stream()
            .filter(old -> !newTagNames.contains(old.getTag().getName())).toList();

        toRemove.forEach(this::removeBookTag);

        Set<String> currentTagNames = this.bookTagList.stream().map(BookTag::getTag)
            .map(Tag::getName).collect(
                Collectors.toSet());

        newTags.stream().filter(newTag -> !currentTagNames.contains(newTag.getName())).forEach(this::addTag);
    }

    private void removeBookTag(BookTag bookTag) {
        this.bookTagList.remove(bookTag);
        bookTag.setBook(null);
    }

    private String createBookAuthorCompositeKey(Author author, String role) {
        return author.getName() + "::" + role;
    }

    public void syncBookAuthors(List<AuthorSyncData> newAuthorDataList) {
        Set<String> newCompositeKeys = newAuthorDataList.stream()
            .map(data -> createBookAuthorCompositeKey(data.author(),
                data.role())).collect(Collectors.toSet());

        List<BookAuthor> toRemove = this.bookAuthorList.stream().filter(old -> {
                String oldKey = createBookAuthorCompositeKey(old.getAuthor(), old.getRole());
                return !newCompositeKeys.contains(oldKey);
            })
            .toList();
        toRemove.forEach(this::removeBookAuthor);

        Set<String> currentCompositeKeys = this.bookAuthorList.stream()
            .map(old -> createBookAuthorCompositeKey(old.getAuthor(),
                old.getRole())).collect(Collectors.toSet());

        newAuthorDataList.stream().filter(newData -> !currentCompositeKeys.contains(createBookAuthorCompositeKey(newData.author(),
                newData.role())))
            .map(newData -> new BookAuthor(this, newData.author(), newData.role()))
            .forEach(this::addBookAuthor);

    }

    private void removeBookAuthor(BookAuthor bookAuthor) {
        this.bookAuthorList.remove(bookAuthor);
        bookAuthor.setBook(null);
    }
}
