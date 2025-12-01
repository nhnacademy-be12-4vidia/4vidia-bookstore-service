package com.nhnacademy._vidiabookstoreservice.book.service;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookTag;
import com.nhnacademy._vidiabookstoreservice.book.domain.Tag;

public interface BookTagService {

    BookTag create(Book book, Tag tag);

    BookTag createByEntity(BookTag bookTag);

}
