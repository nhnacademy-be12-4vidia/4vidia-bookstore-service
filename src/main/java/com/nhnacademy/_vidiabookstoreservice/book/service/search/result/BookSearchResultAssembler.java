package com.nhnacademy._vidiabookstoreservice.book.service.search.result;

import com.nhnacademy._vidiabookstoreservice.book.document.BookDocument;
import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookListResponse;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookSearchListResponse;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import com.nhnacademy._vidiabookstoreservice.user.dto.like.response.UserLikeResponse;
import com.nhnacademy._vidiabookstoreservice.user.service.LikeService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookSearchResultAssembler {

    private final BookRepository bookRepository;
    private final LikeService likeService;

    public Page<BookSearchListResponse> assemble(List<BookDocument> docs, Long userId, Pageable pageable) {
        long total = docs.size();

        if (total == 0) {
            return Page.empty(pageable);
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), docs.size());

        if (start >= docs.size()) {
            return new PageImpl<>(List.of(), pageable, total);
        }

        List<BookDocument> pageDocList = docs.subList(start, end);
        List<Long> bookIdList = pageDocList.stream().map(d -> Long.valueOf(d.getId())).toList();

        List<Book> bookList = bookRepository.findAllById(bookIdList);
        Map<Long, Book> bookMap = bookList.stream()
            .collect(Collectors.toMap(Book::getId, Function.identity()));

        Set<Long> likedBookIds;
        if (userId != null && !bookIdList.isEmpty()) {
            likedBookIds = likeService.getLikeIdList(userId, bookIdList).stream().map(UserLikeResponse::bookId).collect(Collectors.toSet());
        } else {
            likedBookIds = Collections.emptySet();
        }

        List<BookSearchListResponse> responseList = bookIdList.stream()
            .map(bookMap::get)
            .filter(Objects::nonNull)
            .map(b -> BookSearchListResponse.from(
                b, likedBookIds.contains(b.getId())
            ))
            .toList();

        return new PageImpl<>(responseList, pageable, total);
    }

}
