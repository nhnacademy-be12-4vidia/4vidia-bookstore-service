package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.nhnacademy._vidiabookstoreservice.admin.service.AdminBookService;
import com.nhnacademy._vidiabookstoreservice.book.domain.*;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.ImageType;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.StockStatus;
import com.nhnacademy._vidiabookstoreservice.book.dto.author.request.AuthorRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.event.BookSavedEvent;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.event.BookStockChangedEvent;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookStockChangeRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookDetailResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookIdResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookListResponse;
import com.nhnacademy._vidiabookstoreservice.book.exception.already.BookAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.BookNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.ReviewRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.*;
import com.nhnacademy._vidiabookstoreservice.book.service.search.BookSearchService;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.BookOrderResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.like.response.LikeResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@RecordApplicationEvents
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BookServiceImpl.class)
@TestPropertySource(properties = "image.default.thumbnail=http://test-default/thumb.png")
class BookServiceImplTest {

    @Autowired
    BookServiceImpl bookService;

    @MockitoBean
    BookRepository bookRepository;
    @MockitoBean
    AuthorService authorService;
    @MockitoBean
    BookAuthorService bookAuthorService;
    @MockitoBean
    BookImageService bookImageService;
    @MockitoBean
    MinioService minioService;
    @MockitoBean
    PublisherService publisherService;
    @MockitoBean
    CategoryService categoryService;
    @Autowired
    ApplicationEvents applicationEvents;
    @MockitoBean
    TagService tagService;
    @MockitoBean
    ReviewRepository reviewRepository;
    @MockitoBean
    BookTagService bookTagService;
    @MockitoBean
    LikeService likeService;
    @MockitoBean
    BookSearchService bookSearchService;
    @MockitoBean
    DiscountPolicyService discountPolicyService;
    @MockitoBean
    AdminBookService adminBookService;

    @Test
    void createBook_whenIsbnExists_throw() {
        BookCreateRequest req = mock(BookCreateRequest.class);
        when(req.getIsbn()).thenReturn("isbn-1");
        when(bookRepository.existsByIsbn("isbn-1")).thenReturn(true);

        assertThrows(BookAlreadyExistsException.class, () -> bookService.createBook(req, null));

        verify(bookRepository, never()).save(any());
    }

    @Test
    void createBook_success() {
        BookCreateRequest req = mock(BookCreateRequest.class);
        when(req.getIsbn()).thenReturn("test");
        when(bookRepository.existsByIsbn("test")).thenReturn(false);

        when(req.getPublisherName()).thenReturn("publisher");
        Publisher publisher = mock(Publisher.class);
        when(publisherService.getOrCreateByName("publisher")).thenReturn(publisher);

        when(req.getCategoryId()).thenReturn(1L);
        Category category = mock(Category.class);
        when(categoryService.getCategory(1L)).thenReturn(category);

        when(req.getPriceStandard()).thenReturn(20000);
        when(discountPolicyService.calculateSalesPrice(20000, category)).thenReturn(18000);

        Book book = mock(Book.class);
        when(req.toEntity(publisher, category)).thenReturn(book);
        when(book.getPriceStandard()).thenReturn(20000);
        when(book.getStock()).thenReturn(500);
        when(book.isPackagingAvailable()).thenReturn(false);
        when(book.getStockStatus()).thenReturn(null);

        Book savedBook = mock(Book.class);
        when(bookRepository.save(book)).thenReturn(savedBook);
        when(savedBook.getId()).thenReturn(100L);
        when(savedBook.getTitle()).thenReturn("title");

        when(req.getThumbnailUrl()).thenReturn(null);
        BookImage thumbnailImage = mock(BookImage.class);
        when(bookImageService.create(savedBook, "http://test-default/thumb.png", ImageType.THUMBNAIL, 0))
                .thenReturn(thumbnailImage);

        when(req.getAuthorList()).thenReturn(List.of(new AuthorRequest("author", "지은이")));
        when(req.getTagList()).thenReturn("tag");

        Author author = mock(Author.class);
        Tag tag = mock(Tag.class);

        when(authorService.getOrCreateAuthor("author")).thenReturn(author);
        when(tagService.getOrCreateTag("tag")).thenReturn(tag);

        BookAuthor bookAuthor = mock(BookAuthor.class);

        when(bookAuthorService.create(savedBook, author, "지은이")).thenReturn(bookAuthor);

        BookIdResponse res = bookService.createBook(req, null);

        assertNotNull(res);
        assertEquals(100L, res.getId());

        verify(publisherService).getOrCreateByName("publisher");
        verify(categoryService).getCategory(1L);
        verify(discountPolicyService).calculateSalesPrice(20000, category);

        verify(bookRepository).save(book);
        verify(bookImageService).create(savedBook, "http://test-default/thumb.png", ImageType.THUMBNAIL, 0);
        verify(savedBook).addBookImage(thumbnailImage);
        verify(authorService).getOrCreateAuthor("author");
        verify(tagService).getOrCreateTag("tag");

        verify(bookAuthorService).create(savedBook, author, "지은이");
        verify(savedBook).addBookAuthor(bookAuthor);
        verify(savedBook).syncBookTags(argThat(list -> list.size() == 1 && list.get(0) == tag));

    }

    @Test
    void getBookDetail_success() {
        Book book = mock(Book.class, RETURNS_DEEP_STUBS);
        when(book.getStockStatus()).thenReturn(StockStatus.IN_STOCK);
        when(book.getPriceStandard()).thenReturn(200000);
        when(book.getStock()).thenReturn(500);
        when(book.isPackagingAvailable()).thenReturn(false);

        when(bookRepository.findByIdWithAuthors(1L)).thenReturn(Optional.of(book));
        when(reviewRepository.countByBook_Id(1L)).thenReturn(Optional.of(3l));
        when(reviewRepository.findAverageRatingByBookId(1L)).thenReturn(3.0);

        BookDetailResponse response = bookService.getBookDetail(1L);

        assertNotNull(response);

        verify(bookRepository).findByIdWithAuthors(1L);
        verify(reviewRepository).countByBook_Id(1L);
        verify(reviewRepository).findAverageRatingByBookId(1L);
    }

    @Test
    void getBookDetail_withNotExistsId_throw() {
        when(bookRepository.findByIdWithAuthors(1L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.getBookDetail(1L));

        verify(bookRepository).findByIdWithAuthors(1L);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void getBookListByCategory() {

        PageRequest pageable = PageRequest.of(0, 10);

        Book b1 = mock(Book.class, RETURNS_DEEP_STUBS);
        Book b2 = mock(Book.class, RETURNS_DEEP_STUBS);

        Page<Book> bookPage = new PageImpl<>(List.of(b1, b2), pageable, 2);
        when(bookRepository.findByCategoryId(1L, pageable)).thenReturn(bookPage);

        Page<BookListResponse> response = bookService.getBookListByCategory(1L, pageable);

        assertEquals(2, response.getContent().size());
        verify(bookRepository).findByCategoryId(1L, pageable);
    }

    @Test
    void getBookListByPublisher() {
        PageRequest pageable = PageRequest.of(0, 10);

        Book b1 = mock(Book.class, RETURNS_DEEP_STUBS);
        Book b2 = mock(Book.class, RETURNS_DEEP_STUBS);

        Page<Book> bookPage = new PageImpl<>(List.of(b1, b2), pageable, 2);
        when(bookRepository.findByPublisherId(1L, pageable)).thenReturn(bookPage);

        Page<BookListResponse> response = bookService.getBookListByPublisher(1L, pageable);

        assertEquals(2, response.getContent().size());
        verify(bookRepository).findByPublisherId(1L, pageable);

    }

    @Test
    void getBookListByAuthor() {
        PageRequest pageable = PageRequest.of(0, 10);

        Book b1 = mock(Book.class, RETURNS_DEEP_STUBS);
        Book b2 = mock(Book.class, RETURNS_DEEP_STUBS);

        Page<Book> bookPage = new PageImpl<>(List.of(b1, b2), pageable, 2);
        when(bookRepository.findByAuthorId(1L, pageable)).thenReturn(bookPage);

        Page<BookListResponse> response = bookService.getBookListByAuthor(1L, pageable);

        assertEquals(2, response.getContent().size());
        verify(bookRepository).findByAuthorId(1L, pageable);
    }

    @Test
    void getBookListByCategoryPath() {
        PageRequest pageable = PageRequest.of(0, 10);

        Book b1 = mock(Book.class, RETURNS_DEEP_STUBS);
        Book b2 = mock(Book.class, RETURNS_DEEP_STUBS);

        Page<Book> bookPage = new PageImpl<>(List.of(b1, b2), pageable, 2);
        Category category = mock(Category.class);
        when(categoryService.getCategory(1L)).thenReturn(category);
        when(category.getPath()).thenReturn("bla");
        when(bookRepository.findAllByCategoryPath("bla", pageable)).thenReturn(bookPage);

        Page<BookListResponse> response = bookService.getBookListByCategoryPath(1L, pageable);

        assertEquals(2, response.getContent().size());
        verify(categoryService).getCategory(1L);
        verify(bookRepository).findAllByCategoryPath("bla", pageable);
    }

    @Test
    void getOrderBooksByBookIds() {
        Book b1 = mock(Book.class, RETURNS_DEEP_STUBS);
        Book b2 = mock(Book.class, RETURNS_DEEP_STUBS);
        List<Long> idList = List.of(1L, 2L);

        when(bookRepository.findAllByIdIn(idList)).thenReturn(List.of(b1, b2));
        List<BookOrderResponse> response = bookService.getOrderBookByBookIds(idList);

        assertEquals(2, response.size());
        verify(bookRepository).findAllByIdIn(idList);
    }

    @Test
    void decreaseStock() {
        BookStockChangeRequest r1 = new BookStockChangeRequest(1L, 2);
        BookStockChangeRequest r2 = new BookStockChangeRequest(2L, 2);

        Book b1 = mock(Book.class);
        Book b2 = mock(Book.class);

        when(b1.getId()).thenReturn(1L);
        when(b2.getId()).thenReturn(2L);

        when(b1.getStock()).thenReturn(98);
        when(b2.getStock()).thenReturn(48);

        when(bookRepository.findByIdWithLock(1L)).thenReturn(Optional.of(b1));
        when(bookRepository.findByIdWithLock(2L)).thenReturn(Optional.of(b2));

        bookService.decreaseStock(List.of(r1, r2));

        List<BookStockChangedEvent> eventList = applicationEvents.stream(BookStockChangedEvent.class)
                .sorted(Comparator.comparing(BookStockChangedEvent::bookId))
                .toList();

        assertEquals(2, eventList.size());

        assertEquals(1L, eventList.get(0).bookId());
        assertEquals(98, eventList.get(0).newStock());

        assertEquals(2L, eventList.get(1).bookId());
        assertEquals(48, eventList.get(1).newStock());
    }

    @Test
    void increaseStock() {
        BookStockChangeRequest r1 = new BookStockChangeRequest(1L, 2);
        BookStockChangeRequest r2 = new BookStockChangeRequest(2L, 2);

        Book b1 = mock(Book.class);
        Book b2 = mock(Book.class);

        when(b1.getId()).thenReturn(1L);
        when(b2.getId()).thenReturn(2L);

        when(b1.getStock()).thenReturn(102);
        when(b2.getStock()).thenReturn(52);

        when(bookRepository.findByIdWithLock(1L)).thenReturn(Optional.of(b1));
        when(bookRepository.findByIdWithLock(2L)).thenReturn(Optional.of(b2));

        bookService.increaseStock(List.of(r1, r2));

        List<BookStockChangedEvent> eventList = applicationEvents.stream(BookStockChangedEvent.class)
                .sorted(Comparator.comparing(BookStockChangedEvent::bookId))
                .toList();

        assertEquals(2, eventList.size());

        assertEquals(1L, eventList.get(0).bookId());
        assertEquals(102, eventList.get(0).newStock());

        assertEquals(2L, eventList.get(1).bookId());
        assertEquals(52, eventList.get(1).newStock());
    }

    @Test
    void updateBook() {
        Long bookId = 1L;
        BookUpdateRequest request = mock(BookUpdateRequest.class);

        Book book = mock(Book.class, RETURNS_DEEP_STUBS);
        when(book.getIsbn()).thenReturn("isbn-1");
        when(book.getId()).thenReturn(1L);
        when(book.getTitle()).thenReturn("title");
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));

        Publisher publisher = mock(Publisher.class);
        when(request.getPublisherName()).thenReturn("publisher");
        when(publisherService.getOrCreateByName(request.getPublisherName())).thenReturn(publisher);

        Category category = mock(Category.class);
        when(request.getCategoryId()).thenReturn(1L);
        when(categoryService.getCategory(1L)).thenReturn(category);

        when(request.getPriceStandard()).thenReturn(20000);
        when(discountPolicyService.calculateSalesPrice(20000, category)).thenReturn(18000);

        Author author = mock(Author.class);
        when(authorService.getOrCreateAuthor(anyString())).thenReturn(author);

        LocalDate publishedDate = LocalDate.of(2025, 12, 29);
        when(request.getTitle()).thenReturn("title");
        when(request.getSubtitle()).thenReturn("sub");
        when(request.getDescription()).thenReturn("desc");
        when(request.getBookIndex()).thenReturn("idx");
        when(request.getPublishedDate()).thenReturn(publishedDate);
        when(request.getLanguage()).thenReturn("ko");
        when(request.getPageCount()).thenReturn(300);
        when(request.getVolumeNumber()).thenReturn("2");

        when(request.getPriceStandard()).thenReturn(20000);
        when(request.getStock()).thenReturn(500);
        when(request.getPackagingAvailable()).thenReturn(false);
        when(request.getStockStatus()).thenReturn(StockStatus.IN_STOCK);

        when(request.getAuthorList()).thenReturn(List.of(new AuthorRequest("author", "지은이")));
        when(request.getTagList()).thenReturn("tag");
        Tag tag = mock(Tag.class);
        when(tagService.getOrCreateTag("tag")).thenReturn(tag);

        when(request.getThumbnailUrl()).thenReturn("bla");

        bookService.updateBook(bookId, request, null);

        verify(bookRepository).findById(anyLong());
        verify(publisherService).getOrCreateByName("publisher");
        verify(categoryService).getCategory(1L);
        verify(discountPolicyService).calculateSalesPrice(20000, category);
        verify(authorService).getOrCreateAuthor(anyString());

        verify(book).updateBasicInfo(
                eq("title"), eq("sub"), eq("desc"), eq("idx"),
                eq(publishedDate), eq(publisher), eq(category), eq("ko"), eq(300),
                eq(2)
        );

        verify(tagService).getOrCreateTag("tag");
        verify(book).updatePriceAndStock(eq(20000), eq(18000), eq(500), eq(false), eq(StockStatus.IN_STOCK));
        verify(book).syncBookAuthors(argThat(list ->
                list.size() == 1
        && list.get(0).author() == author
        && "지은이".equals(list.get(0).role())));

        verify(bookImageService).replaceThumbnail(book, "bla");
        verify(adminBookService).evictIsbnCaches("isbn-1");

        verify(bookRepository).save(book);

        List<BookSavedEvent> eventList = applicationEvents.stream(BookSavedEvent.class).toList();
        assertEquals(1, eventList.size());
        assertEquals(bookId, eventList.get(0).bookId());
        assertEquals("title", eventList.get(0).title());
    }

    @Test
    void getBookListResponseByIdList_whenUserLoggedIn_setsLikedCorrectly() {
        //given
        List<Long> bookIdList = List.of(1L, 2L);
        Long userId = 1L;

        Book b1 = mock(Book.class);
        Book b2 = mock(Book.class);

        when(b1.getId()).thenReturn(1L);
        when(b2.getId()).thenReturn(2L);

        when(bookRepository.findAllById(bookIdList)).thenReturn(List.of(b1, b2));

        LikeResponse lr1 = mock(LikeResponse.class);
        LikeResponse lr2 = mock(LikeResponse.class);

        when(lr1.bookId()).thenReturn(1L);
        when(lr2.bookId()).thenReturn(2L);

        when(likeService.getLikes(1L)).thenReturn(List.of(lr1, lr2));

        //when
        List<BookListResponse> result = bookService.getBookListResponseByIdList(bookIdList, userId);

        //then
        assertEquals(2, result.size());

        assertTrue(result.get(0).isLiked());
        assertTrue(result.get(1).isLiked());

        verify(bookRepository).findAllById(bookIdList);
        verify(likeService).getLikes(1L);
    }

    @Test
    void getBookListResponseByIdList_whenUserNotLoggedIn_noLiked() {
        //given

        List<Long> bookIdList = List.of(1L, 2L);
        Long userId = null;

        Book b1 = mock(Book.class);
        Book b2 = mock(Book.class);

        when(b1.getId()).thenReturn(1L);
        when(b2.getId()).thenReturn(2L);

        when(bookRepository.findAllById(bookIdList)).thenReturn(List.of(b1, b2));

        //when
        List<BookListResponse> result = bookService.getBookListResponseByIdList(bookIdList, userId);

        //then
        assertEquals(2, result.size());

        assertFalse(result.get(0).isLiked());
        assertFalse(result.get(1).isLiked());

        verify(bookRepository).findAllById(bookIdList);
        verifyNoInteractions(likeService);
    }

    @Test
    void getBookListResponseByTagId_whenUserLoggedInAndBooksExists_setsLikeCorrectly() {
        //given
        PageRequest pageable = PageRequest.of(0, 10);

        Long tagId = 1L;
        Long userId = 1L;

        Book b1 = mock(Book.class);
        Book b2 = mock(Book.class);

        Page<Book> bookPage = new PageImpl<>(List.of(b1, b2), pageable, 2);
        when(bookRepository.findAllByTag(1L, pageable)).thenReturn(bookPage);



    }


}