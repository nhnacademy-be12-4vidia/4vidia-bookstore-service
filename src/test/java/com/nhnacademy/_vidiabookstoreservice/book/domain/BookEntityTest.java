package com.nhnacademy._vidiabookstoreservice.book.domain;

import com.nhnacademy._vidiabookstoreservice.book.domain.enums.StockStatus;
import com.nhnacademy._vidiabookstoreservice.book.exception.invalid.BookStockNotEnoughException;
import com.nhnacademy._vidiabookstoreservice.book.service.impl.BookServiceImpl.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookEntityTest {
    private Book book;
    private Category category;
    private Publisher publisher;

    @BeforeEach
    void init() {

        category = mock(Category.class);
        publisher = mock(Publisher.class);

        book = Book.builder()
                .title("t")
                .isbn("i")
                .stock(5)
                .stockStatus(StockStatus.IN_STOCK)
                .packagingAvailable(false)
                .build();
    }


    @Test
    void decreaseStock_whenEnoughStock_decrease() {
        book.decreaseStock(2);

        assertEquals(3, book.getStock());
        assertEquals(StockStatus.IN_STOCK, book.getStockStatus());
    }

    @Test
    void decreaseStock_whenBecomeZero_setOutOfStock() {
        book.decreaseStock(5);

        assertEquals(0, book.getStock());
        assertEquals(StockStatus.OUT_OF_STOCK, book.getStockStatus());
    }

    @Test
    void decreaseStock_whenNotEnoughStock_throws() {

        assertThrows(BookStockNotEnoughException.class, () -> book.decreaseStock(6));
    }

    @Test
    void updateBasicInfo_updatesAllFields() {
        //given
        LocalDate publishedDate = LocalDate.of(2020, 1, 1);

        //when
        book.updateBasicInfo(
                "new-title",
                "new-subtitle",
                "new-desc",
                "new-index",
                publishedDate,
                publisher,
                category,
                "ko",
                321,
                2
        );

        // then
        assertEquals("new-title", book.getTitle());
        assertEquals("new-subtitle", book.getSubtitle());
        assertEquals("new-desc", book.getDescription());
        assertEquals("new-index", book.getBookIndex());
        assertEquals(publishedDate, book.getPublishedDate());
        assertSame(publisher, book.getPublisher());
        assertSame(category, book.getCategory());
        assertEquals("ko", book.getLanguage());
        assertEquals(321, book.getPageCount());
        assertEquals(2, book.getVolumeNumber());
    }

    @Test
    void updatePriceAndStock_updatesAllFields() {
        book.updatePriceAndStock(
                100000,
                90000,
                500,
                true,
                StockStatus.IN_STOCK
        );

        assertEquals(100000, book.getPriceStandard());
        assertEquals(90000, book.getPriceSales());
        assertEquals(500, book.getStock());
        assertTrue(book.isPackagingAvailable());
        assertEquals(StockStatus.IN_STOCK, book.getStockStatus());
    }

    @Test
    void syncBookTags_removesMissingAndAddsNew() {
        // given
        Tag a = tagNamed("A");
        Tag b = tagNamed("B");
        book.addTag(a);
        book.addTag(b);
        assertEquals(Set.of("A", "B"), currentTagNames());

        // when
        Tag c = tagNamed("C");
        book.syncBookTags(List.of(b, c));

        // then
        assertEquals(Set.of("B", "C"), currentTagNames());
        assertEquals(2, book.getBookTagList().size());
    }

    @Test
    void syncBookTags_whenSameTagProvided_doesNotDuplicate() {
        // given
        Tag a = tagNamed("A");
        book.addTag(a);

        // when
        Tag a2 = tagNamed("A");
        book.syncBookTags(List.of(a2));

        // then
        assertEquals(Set.of("A"), currentTagNames());
        assertEquals(1, book.getBookTagList().size());
    }

    @Test
    void syncBookAuthors_removesMissingAndAddsNew_byCompositeKey() {
        // given
        Author kim = authorNamed("kim");
        Author lee = authorNamed("lee");
        book.addBookAuthor(new BookAuthor(book, kim, "지은이"));
        book.addBookAuthor(new BookAuthor(book, lee, "옮긴이"));
        assertEquals(Set.of("kim::지은이", "lee::옮긴이"), currentAuthorKeys());

        // when
        Author park = authorNamed("park");
        book.syncBookAuthors(List.of(
                new AuthorSyncData(kim, "지은이"),
                new AuthorSyncData(park, "감수")
        ));

        // then
        assertEquals(Set.of("kim::지은이", "park::감수"), currentAuthorKeys());
        assertEquals(2, book.getBookAuthorList().size());
    }

    @Test
    void syncBookAuthors_whenRoleChanges_treatsAsDifferentAuthorEntry() {
        Author kim = authorNamed("kim");
        book.addBookAuthor(new BookAuthor(book, kim, "지은이"));

        book.syncBookAuthors(List.of(new AuthorSyncData(kim, "옮긴이")));

        // then
        assertEquals(Set.of("kim::옮긴이"), currentAuthorKeys());
        assertEquals(1, book.getBookAuthorList().size());
    }

    private Set<String> currentTagNames() {
        return book.getBookTagList().stream()
                .map(BookTag::getTag)
                .map(Tag::getName)
                .collect(Collectors.toSet());
    }

    private Set<String> currentAuthorKeys() {
        return book.getBookAuthorList().stream()
                .map(ba -> ba.getAuthor().getName() + "::" + ba.getRole())
                .collect(Collectors.toSet());
    }

    private Tag tagNamed(String name) {
        Tag t = mock(Tag.class);
        when(t.getName()).thenReturn(name);
        return t;
    }

    private Author authorNamed(String name) {
        Author a = mock(Author.class);
        when(a.getName()).thenReturn(name);
        return a;
    }

}