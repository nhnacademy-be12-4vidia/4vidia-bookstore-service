package com.nhnacademy._vidiabookstoreservice.book.repository.search;

import com.nhnacademy._vidiabookstoreservice.book.document.BookDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BookSearchRepository extends ElasticsearchRepository<BookDocument, String> {

}
