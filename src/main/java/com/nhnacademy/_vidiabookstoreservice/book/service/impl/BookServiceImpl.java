package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Author;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookAuthor;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookImage;
import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import com.nhnacademy._vidiabookstoreservice.book.domain.Publisher;
import com.nhnacademy._vidiabookstoreservice.book.domain.Tag;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.ImageType;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.event.BookSavedEvent;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.event.BookStockChangedEvent;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookSearchRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookStockChangeRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookDetailResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookIdResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookListResponse;
import com.nhnacademy._vidiabookstoreservice.book.exception.already.BookAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.exception.invalid.BookAuthorRequiredException;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.BookNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.ReviewRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.AuthorService;
import com.nhnacademy._vidiabookstoreservice.book.service.BookAuthorService;
import com.nhnacademy._vidiabookstoreservice.book.service.BookImageService;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import com.nhnacademy._vidiabookstoreservice.book.service.BookTagService;
import com.nhnacademy._vidiabookstoreservice.book.service.CategoryService;
import com.nhnacademy._vidiabookstoreservice.book.service.PublisherService;
import com.nhnacademy._vidiabookstoreservice.book.service.TagService;

import java.util.*;
import java.util.stream.Collectors;

import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.BookOrderResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.like.response.LikeResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
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
    private final PublisherService publisherService;
    private final CategoryService categoryService;
    private final ApplicationEventPublisher eventPublisher;
    private final TagService tagService;
    private final ReviewRepository reviewRepository;
    private final BookTagService bookTagService;
    private final LikeService likeService;

    @Value("${image.default.thumbnail}")
    private String defaultThumbnailUrl;

    @Override
    @Transactional
    public BookIdResponse createBook(BookCreateRequest request, MultipartFile thumbnail, List<MultipartFile> detailImages) {

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BookAlreadyExistsException(request.getIsbn());
        }

        Publisher publisher = publisherService.getOrCreateByName(request.getPublisherName());
        Category categoryProxy = categoryService.getCategoryProxy(request.getCategoryId());
        Book book = request.toEntity(publisher, categoryProxy);

        Book savedBook = bookRepository.save(book);

        saveThumbnail(thumbnail, savedBook);
        saveBookImages(detailImages, savedBook);

        saveAuthors(savedBook, request.getAuthorList(), "지은이");
        saveAuthors(savedBook, request.getContributorList(), "기여자/역자");

        BookIdResponse bookIdDto = new BookIdResponse();
        bookIdDto.setId(savedBook.getId());
        eventPublisher.publishEvent(new BookSavedEvent(savedBook.getId(), savedBook.getTitle()));
        return bookIdDto;
    }

    @Override
    @Transactional(readOnly = true)
    public BookDetailResponse getBookDetail(Long id) {

        Book book = bookRepository.findByIdWithAuthors(id).orElseThrow(
            () -> new BookNotFoundException(id));

        Long totalReviewCount = reviewRepository.countByBook_Id(id).orElse(0L);
        Double avgReviewRating = reviewRepository.findAverageRatingByBookId(id);

        return BookDetailResponse.from(book, totalReviewCount, avgReviewRating);
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
        if (images == null || images.isEmpty()) {
//            BookImage detailImage = bookImageService.create(savedBook, defaultThumbnailUrl,
//                ImageType.DETAIL, 1);
//            savedBook.addBookImage(detailImage);
            return;
        }

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
        return bookRepository.findByIdWithAuthors(bookId).orElseThrow(
                () -> new BookNotFoundException(bookId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookOrderResponse> getOrderBookByBookIds(List<Long> bookIds) {
        List<Book> books = bookIds.stream().map(bookId -> bookRepository.findById(bookId).orElse(null)).toList();

        //TODO 해당책에 discount policy 다시 체크해서 판매가 반환
        //TODO 카테고리 아이디 뒤져서 해당 policy 가져와서 적용하는 서비스 불러와서 확인
        return books.stream()
                .map(BookOrderResponse::from)
                .toList();

//        return bookRepository.findById(bookId).orElseThrow(
//            () -> new BookNotFoundException(bookId));
    }

    @Override
    @Transactional(readOnly = true)
    public Book getProxyById(Long bookId) {
        return bookRepository.getReferenceById(bookId);
    }

    @Override
    @Transactional
    public void decreaseStock(List<BookStockChangeRequest> bookStockChangeRequestList) {

        List<BookStockChangedEvent> stockChangedEventList = new ArrayList<>();

        for (BookStockChangeRequest request : bookStockChangeRequestList) {
            Book book = bookRepository.findByIdWithLock(request.getBookId()).orElseThrow(
                () -> new BookNotFoundException(request.getBookId()));

            book.decreaseStock(request.getQuantity());

            stockChangedEventList.add(new BookStockChangedEvent(book.getId(), book.getStock()));

        }

        for (BookStockChangedEvent event : stockChangedEventList) {
            eventPublisher.publishEvent(event);
        }
    }

    @Override
    @Transactional
    public void increaseStock(List<BookStockChangeRequest> bookStockChangeRequestList) {
        List<BookStockChangedEvent> stockChangedEventList = new ArrayList<>();

        for (BookStockChangeRequest request : bookStockChangeRequestList) {
            Book book = bookRepository.findByIdWithLock(request.getBookId()).orElseThrow(
                () -> new BookNotFoundException(request.getBookId()));

            book.increaseStock(request.getQuantity());

            stockChangedEventList.add(new BookStockChangedEvent(book.getId(), book.getStock()));

        }

        for (BookStockChangedEvent event : stockChangedEventList) {
            eventPublisher.publishEvent(event);
        }
    }

    @Override
    @Transactional
    public void updateBook(Long bookId, BookUpdateRequest request, MultipartFile thumbnail) {

        Book book = bookRepository.findById(bookId).orElseThrow(
            () -> new BookNotFoundException(bookId));

        Publisher publisher = publisherService.getOrCreateByName(request.getPublisherName());
        Category category = categoryService.getCategoryProxy(request.getCategoryId());

        book.updateBasicInfo(
            request.getTitle(), request.getSubtitle(), request.getDescription(),
            request.getBookIndex(), request.getPublishedDate(), publisher, category,
            request.getLanguage(), request.getPageCount(),
            parseIntegerSafe(request.getVolumeNumber())
        );

        book.updatePriceAndStock(
            request.getPriceStandard(), request.getPriceSales(), request.getStock(),
            request.getPackagingAvailable(), request.getStockStatus()
        );

        List<AuthorSyncData> targetAuthorList = new ArrayList<>();

        if (StringUtils.hasText(request.getAuthorList())) {
            getTargetAuthorList(targetAuthorList, request.getAuthorList(),
                "지은이");
        }
        if (StringUtils.hasText(request.getContributorList())) {
            getTargetAuthorList(targetAuthorList, request.getContributorList(), "기여자/역자");
        }
        if (!targetAuthorList.isEmpty()) {
            book.syncBookAuthors(targetAuthorList);
        } else {
            book.syncBookAuthors(Collections.emptyList());
        }

        if (StringUtils.hasText(request.getTagList())) {
            List<Tag> targetTagList = getTargetTagList(request.getTagList());
            book.syncBookTags(targetTagList);
        } else {
            throw new BookAuthorRequiredException();
        }
        if (!thumbnail.isEmpty()) {
            bookImageService.replaceThumbnail(book, thumbnail);
        }

        eventPublisher.publishEvent(new BookSavedEvent(book.getId(), book.getTitle()));

    }

    @Override
    @Transactional(readOnly = true)
    public List<BookListResponse> getBookListResponseByIdList(List<Long> bookIdList, Long userId) { // 랭킹 순으로 정렬되있는 bookIdList
        List<Book> bookList = bookRepository.findAllById(bookIdList);

        // 받아온 bookIdList 순서 그대로 넘겨줘야함
        Map<Long, Book> bookMap = bookList.stream()
                .collect(Collectors.toMap(Book::getId, book -> book));

        List<Long> likedBookIds;
        if (userId != null) {
            likedBookIds = likeService.getLikes(userId).stream().map(LikeResponse::bookId).toList();
        } else {
            likedBookIds = Collections.emptyList();
        }


        return bookIdList.stream()
                .map(bookMap::get)
                .filter(Objects::nonNull)
                .map(book -> {
                    boolean isLiked = likedBookIds.contains(book.getId());
                    return BookListResponse.from(book, isLiked);
                })
                .toList();
    }

    private Integer parseIntegerSafe(String value) {
        try {
            return value != null && !value.isBlank() ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<AuthorSyncData> getTargetAuthorList(List<AuthorSyncData> targetAuthorList, String authorListString, String role) {

        String[] authors = authorListString.split(",");
        for (String s : authors) {
            String name = s.trim();
            if (name.isBlank()) continue;
            Author author = authorService.getOrCreateAuthor(name);
            targetAuthorList.add(new AuthorSyncData(author, role));
        }

        return targetAuthorList;
    }

    private List<Tag> getTargetTagList(String tagListString) {
        List<Tag> targetTagList = new ArrayList<>();

        String[] tags = tagListString.split(",");
        for (String s : tags) {
            String cleanName = s.trim();
            if (cleanName.isBlank()) continue;
            Tag tag = tagService.getOrCreateTag(cleanName);
            targetTagList.add(tag);
        }

        return targetTagList;
    }

    public record AuthorSyncData(Author author, String role){}
}
