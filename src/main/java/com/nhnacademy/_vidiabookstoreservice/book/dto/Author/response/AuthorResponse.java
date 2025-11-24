package com.nhnacademy._vidiabookstoreservice.book.dto.Author.response;

import com.nhnacademy._vidiabookstoreservice.book.domain.Author;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuthorResponse {

    private Long id;

    private String name;

    public static AuthorResponse from(Author author) {
        return new AuthorResponse(author.getId(), author.getName());
    }

}
