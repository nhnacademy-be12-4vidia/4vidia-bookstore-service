package com.nhnacademy._vidiabookstoreservice.book.document;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookAuthor;
import jakarta.persistence.Id;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "4vidia-books")
@Getter
@Builder(toBuilder = true)
public class BookDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;

    @Field(type = FieldType.Keyword)
    private String isbn;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String description;

    @Field(type = FieldType.Keyword)
    private List<String> authors;

    @Field(type = FieldType.Keyword)
    private String publisher;

    @Field(type = FieldType.Integer)
    private Integer stock;

    public static BookDocument from(Book book) {
        return BookDocument.builder()
            .id(String.valueOf(book.getId()))
            .title(book.getTitle())
            .isbn(book.getIsbn())
            .description(book.getDescription())
            .authors(book.getBookAuthors().stream().map(ba -> ba.getAuthor().getName()).toList())
            .publisher(book.getPublisher().getName())
            .stock(book.getStock())
            .build();
    }

}
