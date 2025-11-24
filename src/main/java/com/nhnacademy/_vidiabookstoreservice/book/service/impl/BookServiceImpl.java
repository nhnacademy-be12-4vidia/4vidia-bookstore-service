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
import com.nhnacademy._vidiabookstoreservice.book.exception.CategoryNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.AuthorRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookAuthorRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.CategoryRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.PublisherRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.AuthorService;
import com.nhnacademy._vidiabookstoreservice.book.service.BookAuthorService;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorService authorService;
    private final BookAuthorService bookAuthorService;
    private final PublisherRepository publisherRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public BookIdResponse createBook(BookCreateRequest request) {

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BookAlreadyExistsException(
                "이미 존재하는 도서입니다. ISBN : %s".formatted(request.getIsbn()));
        }

        Publisher publisher = getOrSavePublisher(request.getPublisherName());
        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new CategoryNotFoundException("잘못된 카테고리 코드입니다."));
        Book book = request.toEntity(publisher, category);

        if (StringUtils.hasText(request.getImageUrl())) {
            BookImage bookImage = BookImage.builder()
                .imageUrl(request.getImageUrl())
                .book(book)
                .imageType(ImageType.THUMBNAIL)
                .build();

            book.addBookImage(bookImage);
        }

        Book savedBook = bookRepository.save(book);

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
            bookPage = bookRepository.findByCategory_KdcCode(request.getCategoryCode(), pageable);
        } else {
            bookPage = bookRepository.findAll(pageable);
        }
        return bookPage.map(BookListResponse::from);
    }

    private Publisher getOrSavePublisher(String publisherName) {
        if (publisherName == null || publisherName.isBlank()) {
            return null;
        }
        return publisherRepository.findByName(publisherName).orElseGet(() ->
            publisherRepository.save(new Publisher(publisherName.trim())));
    }


    private void saveAuthors(Book book, String nameStr, String role) {
        if (nameStr == null || nameStr.isBlank()) {
            return;
        }
        String[] names = nameStr.split(",");

        for (String name : names) {
            String cleanName = name.trim();
            if (cleanName.isEmpty()) continue;
            Author author = authorService.getOrCreateAuthor(cleanName);

            bookAuthorService.create(book, author, role);
        }
    }
}
