package com.nhnacademy._vidiabookstoreservice.admin.service.impl;

import com.nhnacademy._vidiabookstoreservice.admin.dto.response.AdminIsbnSearchResponse;
import com.nhnacademy._vidiabookstoreservice.admin.service.AdminBookService;
import com.nhnacademy._vidiabookstoreservice.book.ai.gemini.GeminiAnswerService;
import com.nhnacademy._vidiabookstoreservice.book.redis.repository.AiSearchRepository;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminBookServiceImpl implements AdminBookService {

    private final BookRepository bookRepository;
    private final GeminiAnswerService geminiAnswerService;
    private final AiSearchRepository aiSearchRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminIsbnSearchResponse processIsbnSearch(String isbn) {
        return null;
    }
}