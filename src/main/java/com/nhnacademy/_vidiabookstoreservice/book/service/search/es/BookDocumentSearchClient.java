package com.nhnacademy._vidiabookstoreservice.book.service.search.es;

import co.elastic.clients.elasticsearch._types.SortOptions;
import com.nhnacademy._vidiabookstoreservice.book.document.BookDocument;
import com.nhnacademy._vidiabookstoreservice.book.dto.search.request.EsBookSearchRequest;
import com.nhnacademy._vidiabookstoreservice.book.dto.search.request.EsBookSearchWithTagRequest;

import java.util.List;

public interface BookDocumentSearchClient {

    List<BookDocument> search(EsBookSearchRequest request, float[] queryVector, int maxResult);

    List<BookDocument> searchByTag(EsBookSearchWithTagRequest request, int maxResult);

    List<BookDocument> searchByTagOrderByRating(String tagName, boolean asc, int maxResult);

    SortOptions makeSortOptions(EsBookSearchRequest request);
}
