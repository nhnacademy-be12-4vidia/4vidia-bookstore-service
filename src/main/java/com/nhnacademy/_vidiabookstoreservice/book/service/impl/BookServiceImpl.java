package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Author;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookAuthor;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookImage;
import com.nhnacademy._vidiabookstoreservice.book.domain.Category;
import com.nhnacademy._vidiabookstoreservice.book.domain.Publisher;
import com.nhnacademy._vidiabookstoreservice.book.domain.Tag;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.ImageType;
import com.nhnacademy._vidiabookstoreservice.book.dto.author.request.AuthorRequest;
import com.nhnacademy._vidiabookstoreservice.book.service.*;
import com.nhnacademy._vidiabookstoreservice.book.utils.BookSortKey;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.event.BookSavedEvent;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.event.BookStockChangedEvent;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookCreateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookSearchRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookStockChangeRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.request.BookUpdateRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.*;
import com.nhnacademy._vidiabookstoreservice.book.exception.already.BookAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.exception.invalid.BookAuthorRequiredException;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.BookNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.ReviewRepository;

import java.util.*;
import java.util.stream.Collectors;

import com.nhnacademy._vidiabookstoreservice.book.service.search.BookSearchService;
import com.nhnacademy._vidiabookstoreservice.global.dto.PageResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.BookOrderResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.like.response.LikeResponse;
import com.nhnacademy._vidiabookstoreservice.user.dto.like.response.UserLikeResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final BookSearchService bookSearchService;
    private final DiscountPolicyService discountPolicyService;
    private final com.nhnacademy._vidiabookstoreservice.admin.service.AdminBookService adminBookService;

    @Value("${image.default.thumbnail}")
    private String defaultThumbnailUrl;

    @Override
    @Transactional
    public BookIdResponse createBook(BookCreateRequest request, MultipartFile thumbnail) {

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BookAlreadyExistsException(request.getIsbn());
        }

        Publisher publisher = publisherService.getOrCreateByName(request.getPublisherName());
        Category category = categoryService.getCategory(request.getCategoryId());

        Integer calculatedPriceSales = discountPolicyService.calculateSalesPrice(request.getPriceStandard(), category);

        Book book = request.toEntity(publisher, category);
        book.updatePriceAndStock(
            book.getPriceStandard(),
            calculatedPriceSales,
            book.getStock(),
            book.isPackagingAvailable(),
            book.getStockStatus()
        );

        Book savedBook = bookRepository.save(book);
        saveThumbnail(thumbnail, request.getThumbnailUrl(), savedBook);

        saveAuthors(savedBook, request.getAuthorList());

        // 캐시 무효화
        adminBookService.evictIsbnCaches(request.getIsbn());

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
    public void saveAuthors(Book book, List<AuthorRequest> authors) {
        if (authors == null || authors.isEmpty()) {
            return;
        }

        for (AuthorRequest ar : authors) {
            String cleanName = ar.name() != null ? ar.name().trim() : "";
            if (cleanName.isEmpty()) continue;
            
            Author author = authorService.getOrCreateAuthor(cleanName);

            BookAuthor bookAuthor = bookAuthorService.create(book, author, ar.role());
            book.addBookAuthor(bookAuthor);
        }
    }

    public void saveThumbnail(MultipartFile thumbnail, String thumbnailUrlRequest, Book savedBook) {
        String thumbnailUrl;

        if (thumbnail != null && !thumbnail.isEmpty()) {
            thumbnailUrl = minioService.upload(thumbnail);
        } else if (StringUtils.hasText(thumbnailUrlRequest)) {
            thumbnailUrl = thumbnailUrlRequest;
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
        Category category = categoryService.getCategory(request.getCategoryId()); // Proxy -> Real Entity

        Integer calculatedPriceSales = discountPolicyService.calculateSalesPrice(request.getPriceStandard(), category);

        book.updateBasicInfo(
            request.getTitle(), request.getSubtitle(), request.getDescription(),
            request.getBookIndex(), request.getPublishedDate(), publisher, category,
            request.getLanguage(), request.getPageCount(),
            parseIntegerSafe(request.getVolumeNumber())
        );

        book.updatePriceAndStock(
            request.getPriceStandard(), calculatedPriceSales, request.getStock(),
            request.getPackagingAvailable(), request.getStockStatus()
        );

        List<AuthorSyncData> targetAuthorList = new ArrayList<>();
        if (request.getAuthorList() != null) {
            for (AuthorRequest ar : request.getAuthorList()) {
                if (!StringUtils.hasText(ar.name())) continue;
                Author author = authorService.getOrCreateAuthor(ar.name());
                targetAuthorList.add(new AuthorSyncData(author, ar.role()));
            }
        }

        book.syncBookAuthors(targetAuthorList);

        if (StringUtils.hasText(request.getTagList())) {
            List<Tag> targetTagList = getTargetTagList(request.getTagList());
            book.syncBookTags(targetTagList);
        } else {
            throw new BookAuthorRequiredException();
        }

        if (thumbnail != null && !thumbnail.isEmpty()) {
            bookImageService.replaceThumbnail(book, thumbnail);
        } else if (StringUtils.hasText(request.getThumbnailUrl())) {
            bookImageService.replaceThumbnail(book, request.getThumbnailUrl());
        }

        // 캐시 무효화
        adminBookService.evictIsbnCaches(book.getIsbn());

        bookRepository.save(book);
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

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BaseBookListResponse> getBookListResponseByTagId(Long tagId, Long userId, Pageable pageable) {
        Page<Book> bookList = bookRepository.findAllByTag(tagId, pageable);
        List<Long> bookIdList = bookList.getContent().stream().map(Book::getId).toList();

        Set<Long> likedBookIds;

        if (userId != null && !bookIdList.isEmpty()) {
            likedBookIds = likeService.getLikeIdList(userId, bookIdList).stream().map(UserLikeResponse::bookId).collect(Collectors.toSet());
        } else {
            likedBookIds = Collections.emptySet();
        }

        Page<BaseBookListResponse> page = bookList.map(b -> {
            boolean isLiked = likedBookIds.contains(b.getId());
            return BookListResponse.from(b, isLiked);
        });

        return PageResponse.from(page);
    }

    @Override
    public PageResponse<BaseBookListResponse> getBooksByTag(Long tagId, String tagName, BookSortKey sortKey, boolean asc, Pageable pageable, Long userId) {
        if (sortKey.isEsOnly()) {
            return bookSearchService.searchBooksByTagOrderByRating(tagName, asc, pageable, userId);
        }

        Sort sort = switch (sortKey) {
            case PRICE_SALES -> Sort.by(asc ? Sort.Direction.ASC : Sort.Direction.DESC, "priceSales");
            default -> Sort.by(asc ? Sort.Direction.ASC : Sort.Direction.DESC, "publishedDate");
        };
        Pageable resolved = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        return getBookListResponseByTagId(tagId, userId, resolved);
    }

    private Integer parseIntegerSafe(String value) {
        try {
            return value != null && !value.isBlank() ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
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
