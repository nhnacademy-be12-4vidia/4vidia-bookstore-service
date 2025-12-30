package com.nhnacademy._vidiabookstoreservice.book.repository.custom;

import com.nhnacademy._vidiabookstoreservice.book.domain.*;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookListResponse;
import com.nhnacademy._vidiabookstoreservice.user.domain.QLike;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    QBook b = QBook.book;
    QLike l = QLike.like;
    QBookAuthor ba = QBookAuthor.bookAuthor;
    QAuthor a = QAuthor.author;
    QPublisher p = QPublisher.publisher;
    QBookImage bi = QBookImage.bookImage;
    QBookTag bt = QBookTag.bookTag;

    final int targetBooks = 30;
    final int fetchRows = 500;

    @Override
    public List<BookListResponse> getMainPageCategoryBookList(Long categoryId, Long userId) {

        var likedExpr = getLikedExpr(userId);

        List<Tuple> rows = queryFactory
                .select(
                        b.id, b.title, b.isbn, b.priceStandard, b.priceSales, p.name, a.name, bi.imageUrl, likedExpr
                )
                .from(b)
                .leftJoin(b.bookTagList, bt)
                .leftJoin(b.bookImageList, bi)
                .leftJoin(b.publisher, p)
                .leftJoin(b.bookAuthorList, ba).leftJoin(ba.author, a)
                .where(bt.tag.id.eq(categoryId))
                .orderBy(b.publishedDate.desc(), b.id.desc(), bi.id.asc())
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
                    t.get(bi.imageUrl),
                    Boolean.TRUE.equals(t.get(likedExpr))
            ));

            String authorName = t.get(a.name);
            if (authorName != null) acc.authorNames.add(authorName);
            if (acc.imageUrl == null) {
                String img = t.get(bi.imageUrl);
                if (img != null) acc.imageUrl = img;
            }
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
                    .imageUrl(acc.imageUrl)
                    .liked(acc.liked)
                    .build());
        }

        return result;
    }

    @Override
    public List<BookListResponse> getMainPageNoCategoryBookList(Long userId) {

        var likedExpr = getLikedExpr(userId);

        List<Tuple> rows = queryFactory
                .select(b.id, b.title, b.isbn, b.priceStandard, b.priceSales, p.name, a.name, bi.imageUrl, likedExpr)
                .from(b)
                .leftJoin(b.bookImageList, bi)
                .leftJoin(b.bookAuthorList, ba).leftJoin(ba.author, a)
                .leftJoin(b.publisher, p)
                .orderBy(b.id.desc())
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
                    t.get(bi.imageUrl),
                    Boolean.TRUE.equals(t.get(likedExpr))
            ));

            String authorName = t.get(a.name);
            if (authorName != null) acc.authorNames.add(authorName);
            if (acc.imageUrl == null) {
                String img = t.get(bi.imageUrl);
                if (img != null) acc.imageUrl = img;
            }
        }
        List<BookListResponse> result = new ArrayList<>();

        for (Acc acc : map.values()) {
            result.add(BookListResponse.builder()
                    .id(acc.id)
                    .title(acc.title)
                    .isbn(acc.isbn)
                    .priceStandard(acc.priceStandard)
                    .priceSales(acc.priceSales)
                    .publisherName(acc.publisherName)
                    .authorNames(acc.authorNames)
                    .imageUrl(acc.imageUrl)
                    .liked(acc.liked)
                    .build());
        }
        return result;
    }

    private BooleanExpression getLikedExpr(Long userId) {

        return (userId == null)
                ? Expressions.asBoolean(false)
                : JPAExpressions.selectOne()
                .from(l)
                .where(l.book.eq(b), l.user.userId.eq(userId))
                .exists();
    }

    static class Acc {
        Long id;
        String title;
        String isbn;
        Integer priceStandard;
        Integer priceSales;
        String publisherName;
        List<String> authorNames = new ArrayList<>();
        String imageUrl;
        boolean liked;

        Acc(Long id, String title, String isbn,
            Integer priceStandard, Integer priceSales, String publisherName, String imageUrl, boolean liked) {
            this.id = id;
            this.title = title;
            this.isbn = isbn;
            this.priceStandard = priceStandard;
            this.priceSales = priceSales;
            this.publisherName = publisherName;
            this.imageUrl = imageUrl;
            this.liked = liked;
        }
    }
}
