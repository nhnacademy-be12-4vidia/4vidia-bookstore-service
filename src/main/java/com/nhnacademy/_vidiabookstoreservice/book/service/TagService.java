package com.nhnacademy._vidiabookstoreservice.book.service;

import com.nhnacademy._vidiabookstoreservice.book.domain.Tag;
import com.nhnacademy._vidiabookstoreservice.book.dto.tag.response.TagResponse;
import java.util.List;

public interface TagService {

    List<TagResponse> getTagList();

    Tag getTag(Long tagId);

    Tag getTagProxy(Long tagId);

    TagResponse createTag(String tagName);

    Tag getOrCreateTag(String name);

}
