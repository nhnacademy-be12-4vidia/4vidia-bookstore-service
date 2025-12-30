package com.nhnacademy._vidiabookstoreservice.book.ai.rerank;

import com.nhnacademy._vidiabookstoreservice.book.document.BookDocument;

import java.util.List;

public interface BookDocumentReranker {

    List<BookDocument> rerankSafely(String query, List<BookDocument> docs);

}
