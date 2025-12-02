package com.nhnacademy._vidiabookstoreservice.book.service;

import com.nhnacademy._vidiabookstoreservice.book.domain.Publisher;

public interface PublisherService {

    Publisher getOrCreateByName(String publisherName);

}
