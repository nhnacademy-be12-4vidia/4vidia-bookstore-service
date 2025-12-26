package com.nhnacademy._vidiabookstoreservice.book.dto.author.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthorNameRoleResponse (
        String name,
        String role
) {
}