package com.nhnacademy._vidiabookstoreservice.book.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Tag;
import com.nhnacademy._vidiabookstoreservice.book.dto.tag.response.TagResponse;
import com.nhnacademy._vidiabookstoreservice.book.exception.TagNameAlreadyExistsException;
import com.nhnacademy._vidiabookstoreservice.book.exception.TagNotFoundException;
import com.nhnacademy._vidiabookstoreservice.book.repository.TagRepository;
import com.nhnacademy._vidiabookstoreservice.book.service.TagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getTagList() {

        List<Tag> tagList = tagRepository.findAll();
        return tagList.stream().map(TagResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Tag getTag(Long tagId) {
        return tagRepository.findById(tagId).orElseThrow(
            () -> new TagNotFoundException(
                "해당하는 아이디의 태그는 존재하지 않습니다. ID: %d".formatted(tagId)
            )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Tag getTagProxy(Long tagId) {
        return tagRepository.getReferenceById(tagId);
    }

    @Override
    public TagResponse createTag(String tagName) {

        String cleanName = tagName.trim();

        if (cleanName == null || cleanName.isBlank()) {
            throw new IllegalArgumentException("태그 이름은 필수입니다.");
        }

        if (tagRepository.existsByName(cleanName)) {
            throw new TagNameAlreadyExistsException(
                "이미 존재하는 태그입니다. 태그 이름: %s".formatted(cleanName));
        }

        Tag savedTag = tagRepository.save(new Tag(cleanName));
        return TagResponse.from(savedTag);
    }
}
