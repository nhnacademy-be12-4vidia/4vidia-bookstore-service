package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookImage;
import com.nhnacademy._vidiabookstoreservice.book.domain.enums.ImageType;
import com.nhnacademy._vidiabookstoreservice.book.exception.already.ImageAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.repository.BookImageRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class BookImageServiceImplTest {

    @Mock
    private BookImageRepository bookImageRepository;

    @Mock
    private MinioService minioService;

    @InjectMocks
    private BookImageServiceImpl bookImageService;

    @Test
    @DisplayName("도서 이미지 생성 - 성공")
    void create_success() {
        Book book = mock(Book.class);
        String imageUrl = "http://image.com/test.jpg";
        ImageType type = ImageType.DETAIL;

        when(book.getId()).thenReturn(1L);
        when(bookImageRepository.existsByBook_IdAndImageUrl(1L, imageUrl)).thenReturn(false);
        when(bookImageRepository.save(any(BookImage.class))).thenAnswer(inv -> inv.getArgument(0));

        BookImage result = bookImageService.create(book, imageUrl, type, 1);

        assertNotNull(result);
        assertEquals(imageUrl, result.getImageUrl());
        verify(bookImageRepository).save(any(BookImage.class));
    }

    @Test
    @DisplayName("이미 저장된 이미지 URL인 경우 예외 발생")
    void create_alreadyExists_throwsException() {
        Book book = mock(Book.class);
        String imageUrl = "existing.jpg";

        when(book.getId()).thenReturn(1L);
        when(bookImageRepository.existsByBook_IdAndImageUrl(1L, imageUrl)).thenReturn(true);

        assertThrows(ImageAlreadyExistsException.class, () ->
                bookImageService.create(book, imageUrl, ImageType.DETAIL, 1));
    }

    @Test
    @DisplayName("여러 이미지 저장 - 중복 제외 후 저장")
    void saveAll_filtersExistingUrls() {
        BookImage img1 = BookImage.builder().imageUrl("url1").build();
        BookImage img2 = BookImage.builder().imageUrl("url2").build();
        List<BookImage> images = List.of(img1, img2);

        when(bookImageRepository.findExistingUrls(anyList())).thenReturn(Set.of("url1"));

        bookImageService.saveAll(images);

        verify(bookImageRepository).saveAll(argThat(list -> {
            List<BookImage> resultList = (List<BookImage>) list;
            return resultList.size() == 1 && resultList.get(0).getImageUrl().equals("url2");
        }));
    }

    @Test
    @DisplayName("saveAll - 빈 리스트일 때 즉시 리턴")
    void saveAll_empty_returnsImmediately() {
        bookImageService.saveAll(List.of());
        verify(bookImageRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("썸네일 교체 - 기존 이미지가 MinIO 기반이면 삭제 후 새 이미지 업로드")
    void replaceThumbnail_withMultipartFile() {
        Book book = mock(Book.class);
        MultipartFile file = mock(MultipartFile.class);
        BookImage existingThumbnail = spy(BookImage.builder()
                .imageUrl("old-minio-url")
                .imageType(ImageType.THUMBNAIL)
                .build());

        when(book.getBookImageList()).thenReturn(List.of(existingThumbnail));
        when(minioService.isMinioUrl("old-minio-url")).thenReturn(true);
        when(minioService.upload(file)).thenReturn("new-minio-url");

        bookImageService.replaceThumbnail(book, file);

        verify(minioService).delete("old-minio-url");
        verify(existingThumbnail).updateImageUrl("new-minio-url");
    }

    @Test
    @DisplayName("썸네일 교체 - 기존 이미지가 없을 때 새로 생성")
    void replaceThumbnail_noExisting_createNew() {
        Book book = mock(Book.class);
        when(book.getBookImageList()).thenReturn(new ArrayList<>());
        when(minioService.upload(any(MultipartFile.class))).thenReturn("new-url");

        bookImageService.replaceThumbnail(book, mock(MultipartFile.class));

        verify(bookImageRepository).save(any(BookImage.class));
        verify(book).addBookImage(any(BookImage.class));
    }

    @Test
    @DisplayName("replaceThumbnail(String) - 기존 이미지 있고 MinIO URL인 경우 삭제 후 업데이트")
    void replaceThumbnail_string_existingMinio_success() {
        Book book = mock(Book.class);
        BookImage existing = spy(BookImage.builder().imageUrl("old").imageType(ImageType.THUMBNAIL).build());
        when(book.getBookImageList()).thenReturn(List.of(existing));
        when(minioService.isMinioUrl("old")).thenReturn(true);

        bookImageService.replaceThumbnail(book, "new-url");

        verify(minioService).delete("old");
        verify(existing).updateImageUrl("new-url");
    }

    @Test
    @DisplayName("replaceThumbnail(String) - 기존 이미지 없을 때 신규 생성")
    void replaceThumbnail_string_noExisting_success() {
        Book book = mock(Book.class);
        when(book.getBookImageList()).thenReturn(List.of());

        bookImageService.replaceThumbnail(book, "new-url");

        verify(bookImageRepository).save(any());
        verify(book).addBookImage(any());
    }
}