package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Publisher;
import com.nhnacademy._vidiabookstoreservice.book.repository.PublisherRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.PublisherService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublisherServiceImpl implements PublisherService {

    private final PublisherRepository publisherRepository;

    @Override
    @Transactional
    public Publisher getOrCreateByName(String publisherName) {

        String cleanName = publisherName.trim();

        Optional<Publisher> existing = publisherRepository.findByName(cleanName);

        if (existing.isPresent()) {
            return existing.get();
        }

        try {
            return publisherRepository.save(new Publisher(cleanName));
        } catch (DataIntegrityViolationException e) {
            return publisherRepository.findByName(cleanName)
                .orElseThrow(() -> new RuntimeException("알 수 없는 오류"));
        }
    }
}

