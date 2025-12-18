package com.nhnacademy._vidiabookstoreservice.book.service.search.es;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.KnnSearch;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.nhnacademy._vidiabookstoreservice.book.document.BookDocument;

import com.nhnacademy._vidiabookstoreservice.book.dto.search.request.EsBookSearchRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nhnacademy._vidiabookstoreservice.book.dto.search.request.EsBookSearchWithTagRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchBookDocumentSearchClient implements BookDocumentSearchClient{

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public List<BookDocument> search(EsBookSearchRequest request, float[] queryVector, int maxResult) {
        String keyword = request.getKeyword();
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }

        Query lexicalQuery = buildLexicalQuery(keyword);

        NativeQueryBuilder queryBuilder = new NativeQueryBuilder()
            .withQuery(lexicalQuery)
            .withPageable(PageRequest.of(0, maxResult));

        if (queryVector != null && queryVector.length > 0) {
            KnnSearch knnSearch = KnnSearch.of(k -> k
                .field("embedding")
                .queryVector(toFloatList(queryVector))
                .k(maxResult)
                .numCandidates(maxResult * 5)
            );
            queryBuilder.withKnnSearches(Collections.singletonList(knnSearch));
        }

        NativeQuery nativeQuery = queryBuilder.build();

        SearchHits<BookDocument> searchHits = elasticsearchOperations.search(nativeQuery,
            BookDocument.class);

        return searchHits.stream().map(SearchHit::getContent).toList();

    }

    @Override
    public List<BookDocument> searchByTag(EsBookSearchWithTagRequest request, int maxResult) {
        List<String> tagNameList = request.getTagNameList();
        if (tagNameList == null || tagNameList.isEmpty()) {
            return List.of();
        }

        Query tagQuery = buildTagQuery(request);

        NativeQuery nativeQuery = new NativeQueryBuilder()
                .withQuery(tagQuery)
                .withPageable(PageRequest.of(0, maxResult))
                .build();

        SearchHits<BookDocument> hits = elasticsearchOperations.search(nativeQuery, BookDocument.class);

        return hits.stream().map(SearchHit::getContent).toList();
    }

    private Query buildTagQuery(EsBookSearchWithTagRequest request) {
        List<String> tags = request.getTagNameList();

        EsBookSearchWithTagRequest.MatchMode mode = request.getMode();
        if (mode == null) {
            mode = EsBookSearchWithTagRequest.MatchMode.OR;
        }

        final String tagField = "tags_keyword.keyword";

        if (mode == EsBookSearchWithTagRequest.MatchMode.AND) {
            return Query.of(q -> q.bool(b -> {
                for (String t : tags) {
                    if (!StringUtils.hasText(t)) continue;
                    b.must(m -> m.term(tt -> tt.field(tagField).value(t)));
                }
                return b;
            }));
        }

        List<String> cleaned = tags.stream().filter(StringUtils::hasText).toList();
        return Query.of(q -> q.bool(b -> b
                .filter(f -> f.terms(t -> t.field(tagField).terms(v ->
                        v.value(cleaned.stream().map(FieldValue::of).toList()))))));

    }

    private Query buildLexicalQuery(String keyword) {
        return Query.of(q -> q.bool(b -> b
            .should(s -> s.match(m -> m.field("title").query(keyword).boost(100.0f)))
            .should(s -> s.match(m -> m.field("authors").query(keyword).boost(90.0f)))
            .should(s -> s.match(m -> m.field("tags").query(keyword).boost(80.0f)))
            .should(s -> s.term(t -> t.field("isbn").value(keyword).boost(1000.0f)))
            .should(s -> s.match(m -> m.field("publisher").query(keyword).boost(60.0f)))
            .should(s -> s.match(m -> m.field("description").query(keyword).boost(50.0f)))
            .minimumShouldMatch("1")
        ));
    }

    private List<Float> toFloatList(float[] floats) {
        if (floats == null) return List.of();
        List<Float> list = new ArrayList<>(floats.length);
        for (float f : floats) {
            list.add(f);
        }
        return list;
    }
}
