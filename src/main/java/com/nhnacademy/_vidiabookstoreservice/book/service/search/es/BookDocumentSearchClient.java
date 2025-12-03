package com.nhnacademy._vidiabookstoreservice.book.service.search.es;

import com.nhnacademy._vidiabookstoreservice.book.document.BookDocument;

import com.nhnacademy._vidiabookstoreservice.book.dto.search.request.EsBookSearchRequest;
import java.util.List;

public interface BookDocumentSearchClient {

    List<BookDocument> search(EsBookSearchRequest request, float[] queryVector, int maxResult);

}
