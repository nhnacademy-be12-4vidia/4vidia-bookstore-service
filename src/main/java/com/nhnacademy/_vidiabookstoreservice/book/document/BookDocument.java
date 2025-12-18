package com.nhnacademy._vidiabookstoreservice.book.document;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.domain.BookAuthor;
import jakarta.persistence.Id;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.*;

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

    @Field(type = FieldType.Integer)
    private Integer priceSales;
    
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori"),
            otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword)
    )
    private List<String> tags;

    @Field(type = FieldType.Dense_Vector, dims = 1024)
    private double[] embedding;

    @Field(type = FieldType.Double)
    private Double rating;

    public static BookDocument from(Book book, double[] vector) {

        return BookDocument.builder()
            .id(String.valueOf(book.getId()))
            .title(book.getTitle())
            .isbn(book.getIsbn())
            .description(book.getDescription())
            .priceSales(book.getPriceSales())
            .authors(book.getBookAuthorList().stream().map(ba -> ba.getAuthor().getName()).toList())
            .tags(book.getBookTagList().stream().map(bt -> bt.getTag().getName()).toList())
            .publisher(book.getPublisher().getName())
            .stock(book.getStock())
            .embedding(vector)
            .build();
    }

}
