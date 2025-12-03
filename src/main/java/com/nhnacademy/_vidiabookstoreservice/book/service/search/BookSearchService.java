package com.nhnacademy._vidiabookstoreservice.book.service.search;

import com.nhnacademy._vidiabookstoreservice.book.document.BookDocument;
import com.nhnacademy._vidiabookstoreservice.book.dto.book.response.BookListResponse;

import com.nhnacademy._vidiabookstoreservice.book.dto.search.request.EsBookSearchRequest;
import com.nhnacademy._vidiabookstoreservice.book.service.search.embedding.EmbeddingService;
import com.nhnacademy._vidiabookstoreservice.book.service.search.es.BookDocumentSearchClient;
import com.nhnacademy._vidiabookstoreservice.book.service.search.rerank.BookDocumentReranker;
import com.nhnacademy._vidiabookstoreservice.book.service.search.result.BookSearchResultAssembler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BookSearchService {

    private static final int MAX_RESULTS = 50;

    private final EmbeddingService embeddingService;
    private final BookDocumentSearchClient searchClient;
    private final BookDocumentReranker reranker;
    private final BookSearchResultAssembler resultAssembler;

    public Page<BookListResponse> searchBooks(EsBookSearchRequest request, Pageable pageable) {
        String keyword = request.getKeyword();
        if (!StringUtils.hasText(keyword)) {
            return Page.empty(pageable);
        }

        float[] queryVector = embeddingService.embedOrNull(keyword);

        List<BookDocument> initialDocs = searchClient.search(request, queryVector, MAX_RESULTS);

        if (initialDocs.isEmpty()) {
            return Page.empty(pageable);
        }

        List<BookDocument> rerankDocs = reranker.rerankSafely(keyword, initialDocs);

        return resultAssembler.assemble(rerankDocs, pageable);
    }

}