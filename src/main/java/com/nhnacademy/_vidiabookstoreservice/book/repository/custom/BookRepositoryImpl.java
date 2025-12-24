package com.nhnacademy._vidiabookstoreservice.book.repository.custom;

import com.nhnacademy._vidiabookstoreservice.book.domain.QAuthor;
import com.nhnacademy._vidiabookstoreservice.book.domain.QBook;
import com.nhnacademy._vidiabookstoreservice.book.domain.QBookAuthor;
import com.nhnacademy._vidiabookstoreservice.book.domain.QPublisher;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookListResponse;
import com.nhnacademy._vidiabookstoreservice.user.domain.QLike;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    QBook b = QBook.book;
    QLike l = QLike.like;
    QBookAuthor ba = QBookAuthor.bookAuthor;
    QAuthor a = QAuthor.author;
    QPublisher p = QPublisher.publisher;

    final int targetBooks = 50;
    final int fetchRows = 500;

    @Override
    public List<BookListResponse> getMainPageResult(Long categoryId, Long userId) {

        var likedExpr = JPAExpressions.selectOne().from(l).where(l.book.eq(b), l.user.userId.eq(userId)).exists();

        List<Tuple> rows = queryFactory
                .select(
                        b.id, b.title, b.isbn, b.priceStandard, b.priceSales, p.name, a.name, likedExpr
                )
                .from(b)
                .leftJoin(b.publisher, p)
                .leftJoin(b.bookAuthorList, ba).leftJoin(ba.author, a)
                .where(b.category.id.eq(categoryId))
                .orderBy(b.publishedDate.desc(), b.id.desc())
                .limit(fetchRows)
                .fetch();

        Map<Long, Acc> map = new LinkedHashMap<>();


        for (Tuple t : rows) {
            Long id = t.get(b.id);

            if (!map.containsKey(id) && map.size() >= targetBooks) continue;

            Acc acc = map.computeIfAbsent(id, _id -> new Acc(
                    _id,
                    t.get(b.title),
                    t.get(b.isbn),
                    t.get(b.priceStandard),
                    t.get(b.priceSales),
                    t.get(p.name),
                    t.get(likedExpr)
            ));

            String authorName = t.get(a.name);
            if (authorName != null) acc.authorNames.add(authorName);
        }

        List<BookListResponse> result = new ArrayList<>(map.size());
        for (Acc acc : map.values()) {
            result.add(BookListResponse.builder()
                    .id(acc.id)
                    .title(acc.title)
                    .isbn(acc.isbn)
                    .priceStandard(acc.priceStandard)
                    .priceSales(acc.priceSales)
                    .publisherName(acc.publisherName)
                    .authorNames(acc.authorNames)
                    .liked(acc.liked)
                    .build());
        }

        return result;
    }

    static class Acc {
        Long id;
        String title;
        String isbn;
        Integer priceStandard;
        Integer priceSales;
        String publisherName;
        List<String> authorNames = new ArrayList<>();
        boolean liked;

        Acc(Long id, String title, String isbn,
            Integer priceStandard, Integer priceSales, String publisherName, boolean liked) {
            this.id = id;
            this.title = title;
            this.isbn = isbn;
            this.priceStandard = priceStandard;
            this.priceSales = priceSales;
            this.publisherName = publisherName;
            this.liked = liked;
        }
    }
}
