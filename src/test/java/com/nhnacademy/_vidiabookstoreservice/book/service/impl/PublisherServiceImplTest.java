package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.nhnacademy._vidiabookstoreservice.book.domain.Publisher;
import com.nhnacademy._vidiabookstoreservice.book.repository.PublisherRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class PublisherServiceImplTest {

    @Mock
    private PublisherRepository publisherRepository;

    @InjectMocks
    private PublisherServiceImpl publisherService;

    @Test
    @DisplayName("출판사 조회 및 생성 - 이미 존재하는 경우 조회 결과 반환")
    void getOrCreateByName_existing_returnsExisting() {
        String name = " NHN Academy ";
        String cleanName = "NHN Academy";
        Publisher existingPublisher = new Publisher(cleanName);

        when(publisherRepository.findByName(cleanName)).thenReturn(Optional.of(existingPublisher));

        Publisher result = publisherService.getOrCreateByName(name);

        assertEquals(cleanName, result.getName());
        verify(publisherRepository, never()).save(any(Publisher.class));
    }

    @Test
    @DisplayName("출판사 조회 및 생성 - 존재하지 않는 경우 새로 생성하여 저장")
    void getOrCreateByName_notExisting_createsNew() {
        String name = "New Publisher";
        when(publisherRepository.findByName(name)).thenReturn(Optional.empty());
        when(publisherRepository.save(any(Publisher.class))).thenAnswer(inv -> inv.getArgument(0));

        Publisher result = publisherService.getOrCreateByName(name);

        assertNotNull(result);
        assertEquals(name, result.getName());
        verify(publisherRepository).save(any(Publisher.class));
    }

    @Test
    @DisplayName("출판사 조회 및 생성 - 동시성 이슈로 저장 실패 시 재조회 성공")
    void getOrCreateByName_concurrencyIssue_returnsOnRetry() {
        String name = "Concurrent Publisher";

        Publisher otherSavedPublisher = new Publisher(name);
        when(publisherRepository.findByName(name))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(otherSavedPublisher));

        when(publisherRepository.save(any(Publisher.class)))
                .thenThrow(DataIntegrityViolationException.class);

        Publisher result = publisherService.getOrCreateByName(name);

        assertNotNull(result);
        assertEquals(name, result.getName());
        verify(publisherRepository, times(2)).findByName(name);
        verify(publisherRepository, times(1)).save(any(Publisher.class));
    }

    @Test
    @DisplayName("출판사 조회 및 생성 - 재조회도 실패할 경우 런타임 예외 발생")
    void getOrCreateByName_retryFails_throwsException() {
        String name = "Error Publisher";
        when(publisherRepository.findByName(name)).thenReturn(Optional.empty());
        when(publisherRepository.save(any(Publisher.class))).thenThrow(DataIntegrityViolationException.class);

        when(publisherRepository.findByName(name)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> publisherService.getOrCreateByName(name));
    }
}