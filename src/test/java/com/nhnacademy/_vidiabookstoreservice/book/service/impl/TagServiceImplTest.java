package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.nhnacademy._vidiabookstoreservice.book.domain.Tag;
import com.nhnacademy._vidiabookstoreservice.book.dto.tag.response.TagResponse;
import com.nhnacademy._vidiabookstoreservice.book.exception.already.TagNameAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.exception.notfound.TagNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.TagRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagServiceImpl tagService;

    @Test
    @DisplayName("태그 목록 조회 - 성공")
    void getTagList_success() {
        Tag tag1 = new Tag("태그1");
        Tag tag2 = new Tag("태그2");
        when(tagRepository.findAll()).thenReturn(List.of(tag1, tag2));

        List<TagResponse> result = tagService.getTagList();

        assertEquals(2, result.size());
        verify(tagRepository).findAll();
    }

    @Test
    @DisplayName("태그 단건 조회 - 성공")
    void getTag_success() {
        Long tagId = 1L;
        Tag tag = new Tag("테스트태그");
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));

        Tag result = tagService.getTag(tagId);

        assertEquals("테스트태그", result.getName());
    }

    @Test
    @DisplayName("태그 단건 조회 - 존재하지 않을 경우 예외 발생")
    void getTag_notFound_throwsException() {
        when(tagRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(TagNotFoundException.class, () -> tagService.getTag(1L));
    }

    @Test
    @DisplayName("태그 생성 - 성공 (공백 제거 확인)")
    void createTag_success() {
        String inputName = "  신간도서  ";
        String cleanName = "신간도서";
        when(tagRepository.existsByName(cleanName)).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));

        TagResponse result = tagService.createTag(inputName);

        assertNotNull(result);
        verify(tagRepository).save(argThat(tag -> tag.getName().equals(cleanName)));
    }

    @Test
    @DisplayName("태그 생성 - 이미 존재할 경우 예외 발생")
    void createTag_alreadyExists_throwsException() {
        String tagName = "중복태그";
        when(tagRepository.existsByName(tagName)).thenReturn(true);

        assertThrows(TagNameAlreadyExistsException.class, () -> tagService.createTag(tagName));
    }

    @Test
    @DisplayName("createTag - 빈 이름일 경우 예외 발생")
    void createTag_blank_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> tagService.createTag("  "));
    }

    @Test
    @DisplayName("태그 조회 또는 생성 - 이미 있으면 조회 결과 반환")
    void getOrCreateTag_exists_returnsFound() {
        String tagName = "기존태그";
        Tag existingTag = new Tag(tagName);
        when(tagRepository.findByName(tagName)).thenReturn(Optional.of(existingTag));

        Tag result = tagService.getOrCreateTag(tagName);

        assertEquals(existingTag, result);
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    @DisplayName("태그 조회 또는 생성 - 없으면 새로 생성")
    void getOrCreateTag_notExists_createsNew() {
        String tagName = "새로운태그";
        when(tagRepository.findByName(tagName)).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));

        Tag result = tagService.getOrCreateTag(tagName);

        assertEquals(tagName, result.getName());
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    @DisplayName("getTagProxy - getReferenceById 호출 확인")
    void getTagProxy_success() {
        tagService.getTagProxy(1L);
        verify(tagRepository).getReferenceById(1L);
    }
}