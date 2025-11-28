package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Author;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookAuthor;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookImage;
import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import com.nhnacademy._vidiabookstoreservice.book.domain.Publisher;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.ImageType;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookSearchRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookDetailResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookIdResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookListResponse;
import com.nhnacademy._vidiabookstoreservice.book.exception.BookAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.exception.BookNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.PublisherRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.AuthorService;
import com.nhnacademy._vidiabookstoreservice.book.service.BookAuthorService;
import com.nhnacademy._vidiabookstoreservice.book.service.BookImageService;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import com.nhnacademy._vidiabookstoreservice.book.service.CategoryService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorService authorService;
    private final BookAuthorService bookAuthorService;
    private final BookImageService bookImageService;
    private final MinioService minioService;
    private final PublisherRepository publisherRepository;
    private final CategoryService categoryService;

    @Value("${image.default.thumbnail}")
    private String defaultThumbnailUrl;

    @Override
    @Transactional
    public BookIdResponse createBook(BookCreateRequest request, MultipartFile thumbnail, List<MultipartFile> detailImages) {

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BookAlreadyExistsException(
                "이미 존재하는 도서입니다. ISBN : %s".formatted(request.getIsbn()));
        }

        Publisher publisher = getOrSavePublisher(request.getPublisherName());
        Category categoryProxy = categoryService.getCategoryProxy(request.getCategoryId());
        Book book = request.toEntity(publisher, categoryProxy);

        Book savedBook = bookRepository.save(book);

        saveThumbnail(thumbnail, savedBook);
        saveBookImages(detailImages, savedBook);

        saveAuthors(savedBook, request.getAuthorList(), "지은이");
        saveAuthors(savedBook, request.getContributorList(), "기여자/역자");

        BookIdResponse bookIdDto = new BookIdResponse();
        bookIdDto.setId(savedBook.getId());
        return bookIdDto;
    }

    @Override
    @Transactional(readOnly = true)
    public BookDetailResponse getBookDetail(Long id) {

        Book book = bookRepository.findByIdWithAuthors(id).orElseThrow(
            () -> new BookNotFoundException("ID에 해당하는 도서를 찾을 수 없습니다. ID: %d".formatted(id)));

        return BookDetailResponse.from(book);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookListResponse> getBookList(BookSearchRequest request, Pageable pageable) {

        Page<Book> bookPage;
        if (StringUtils.hasText(request.getKeyword())) {
            bookPage = bookRepository.findByTitleContaining(request.getKeyword(), pageable);
        } else if (StringUtils.hasText(request.getCategoryCode())) {
            bookPage = bookRepository.findByCategoryKdcCode(request.getCategoryCode(), pageable);
        } else {
            bookPage = bookRepository.findAll(pageable);
        }
        return bookPage.map(BookListResponse::from);
    }

    @Transactional
    public Publisher getOrSavePublisher(String publisherName) {
        if (publisherName == null || publisherName.isBlank()) {
            return null;
        }
        return publisherRepository.findByName(publisherName).orElseGet(() ->
            publisherRepository.save(new Publisher(publisherName.trim())));
    }

    @Transactional
    public void saveAuthors(Book book, String nameStr, String role) {
        if (nameStr == null || nameStr.isBlank()) {
            return;
        }
        String[] names = nameStr.split(",");

        for (String name : names) {
            String cleanName = name.trim();
            if (cleanName.isEmpty()) continue;
            Author author = authorService.getOrCreateAuthor(cleanName);

            BookAuthor bookAuthor = bookAuthorService.create(book, author, role);
            book.addBookAuthor(bookAuthor);
        }
    }

    @Transactional
    public void saveThumbnail(MultipartFile thumbnail, Book savedBook) {
        String thumbnailUrl;

        if (thumbnail != null && !thumbnail.isEmpty()) {
            thumbnailUrl = minioService.upload(thumbnail);
        } else {
            thumbnailUrl = defaultThumbnailUrl;
        }

        BookImage thumbnailImage = bookImageService.create(savedBook, thumbnailUrl, ImageType.THUMBNAIL, 0);
        savedBook.addBookImage(thumbnailImage);
    }

    @Transactional
    public void saveBookImages(List<MultipartFile> images, Book savedBook) {
        if (images == null || images.isEmpty()) return;

        Set<String> uniqueFileCheck = new HashSet<>();

        List<BookImage> imageEntities = new ArrayList<>();

        int order = 1;

        for (MultipartFile file : images) {
            if (file.isEmpty()) continue;

            String duplicateKey = file.getOriginalFilename() + "_" + file.getSize();

            if (!uniqueFileCheck.add(duplicateKey)) {
                continue;
            }

            String imageUrl = minioService.upload(file);

            BookImage bookImage = BookImage.builder()
                .book(savedBook)
                .imageUrl(imageUrl)
                .imageType(ImageType.DETAIL)
                .displayOrder(order++)
                .build();

            imageEntities.add(bookImage);
            savedBook.addBookImage(bookImage);
        }
        bookImageService.saveAll(imageEntities);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookListResponse> getBookListByCategory(Long categoryId, Pageable pageable) {

        Page<Book> bookPage = bookRepository.findByCategoryId(categoryId, pageable);

        return bookPage.map(BookListResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookListResponse> getBookListByPublisher(Long publisherId, Pageable pageable) {

        Page<Book> bookPage = bookRepository.findByPublisherId(publisherId, pageable);
        return bookPage.map(BookListResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookListResponse> getBookListByAuthor(Long authorId, Pageable pageable) {

        Page<Book> bookPage = bookRepository.findByAuthorId(authorId, pageable);
        return bookPage.map(BookListResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookListResponse> getBookListByCategoryPath(Long categoryId, Pageable pageable) {

        Category category = categoryService.getCategory(categoryId);

        Page<Book> bookPage = bookRepository.findAllByCategoryPath(category.getPath(), pageable);
        return bookPage.map(BookListResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Book getBookEntity(Long bookId) {
        return bookRepository.findById(bookId).orElseThrow(
            () -> new BookNotFoundException("ID에 해당하는 도서를 찾을 수 없습니다. ID: %d".formatted(bookId)));
    }

    @Override
    public Book getProxyById(Long bookId) {
        return bookRepository.getReferenceById(bookId);
    }
}
